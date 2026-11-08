const crypto = require('crypto');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const env = require('../../config/env');
const db = require('../../config/db');
const ApiError = require('../../utils/ApiError');
const { ageFromBirthday } = require('../../utils/age');
const usersService = require('../users/users.service');
const otpService = require('../../services/otp.service');

const sha256 = (s) => crypto.createHash('sha256').update(s).digest('hex');

function signAccessToken(user) {
  return jwt.sign({ sub: String(user.id), role: user.role }, env.jwt.accessSecret, {
    expiresIn: env.jwt.accessExpiresIn,
  });
}

async function issueTokens(user, conn = db) {
  const refreshToken = crypto.randomBytes(48).toString('hex');
  const expiresAt = new Date(Date.now() + env.jwt.refreshTokenDays * 24 * 3600 * 1000);
  await conn.query('INSERT INTO refresh_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)', [
    user.id,
    sha256(refreshToken),
    expiresAt,
  ]);
  return {
    accessToken: signAccessToken(user),
    refreshToken,
    tokenType: 'Bearer',
    expiresIn: env.jwt.accessExpiresIn,
  };
}

/**
 * Tiêu chí tìm kiếm mặc định suy ra từ thông tin đăng ký:
 *  - interestedIn: người dùng chọn lúc đăng ký, không chọn thì 'everyone'
 *  - độ tuổi: quanh tuổi của người dùng (±5, tối thiểu 18)
 *  - khoảng cách: 50km
 */
function defaultPreferences(birthday, interestedIn) {
  if (!birthday) return { interestedIn: interestedIn || 'everyone', minAge: 18, maxAge: 35, maxDistanceKm: 50 };
  const age = ageFromBirthday(birthday);
  return {
    interestedIn: interestedIn || 'everyone',
    minAge: Math.max(18, age - 5),
    maxAge: Math.min(100, Math.max(age + 5, 23)),
    maxDistanceKm: 50,
  };
}

async function register(data) {
  const { email, password, displayName, birthday, gender } = data;
  const [existing] = await db.query('SELECT id FROM users WHERE email = ?', [email]);
  if (existing) throw ApiError.conflict('Email đã được sử dụng');

  const passwordHash = await bcrypt.hash(password, 10);
  const pref = defaultPreferences(birthday, data.interestedIn);

  const user = await db.transaction(async (conn) => {
    const [result] = await conn.query('INSERT INTO users (email, password_hash) VALUES (?, ?)', [email, passwordHash]);
    const userId = result.insertId;
    await conn.query(
      // Đăng ký email đã có đủ ngày sinh + giới tính -> coi như hoàn tất hồ sơ (luồng test nhanh / web)
      `INSERT INTO profiles (user_id, display_name, birthday, gender, city, latitude, longitude, completed_at)
       VALUES (?, ?, ?, ?, ?, ?, ?, UTC_TIMESTAMP())`,
      [userId, displayName, birthday, gender, data.city || null, data.latitude ?? null, data.longitude ?? null],
    );
    await conn.query(
      'INSERT INTO preferences (user_id, interested_in, min_age, max_age, max_distance_km) VALUES (?, ?, ?, ?, ?)',
      [userId, pref.interestedIn, pref.minAge, pref.maxAge, pref.maxDistanceKm],
    );
    if (data.interestIds?.length) {
      // Bỏ qua id sở thích không tồn tại
      await conn.query(
        'INSERT IGNORE INTO user_interests (user_id, interest_id) SELECT ?, id FROM interests WHERE id IN (?)',
        [userId, [...new Set(data.interestIds)]],
      );
    }
    return { id: userId, role: 'user' };
  });

  const tokens = await issueTokens(user);
  return { user: await usersService.getMe(user.id), tokens };
}

// ═════════════════════ SỐ ĐIỆN THOẠI + OTP ═════════════════════

/** Mật khẩu đặt lại dùng secret riêng -> không thể dùng thay access token. */
const resetSecret = () => `${env.jwt.accessSecret}:reset_password`;
const passwordFingerprint = (hash) => sha256(hash).slice(0, 16);

async function findByPhone(phone) {
  const [user] = await db.query('SELECT id, role, status, password_hash FROM users WHERE phone = ?', [phone]);
  return user || null;
}

/**
 * POST /auth/otp/send - gửi (hoặc gửi lại) OTP.
 *  register       : số chưa được đăng ký
 *  login          : chỉ GỬI LẠI được sau khi đã nhập đúng mật khẩu ở /auth/login/phone (trong 10 phút)
 *  reset_password : số đã đăng ký
 */
async function sendOtp({ phone, purpose }) {
  const user = await findByPhone(phone);
  if (purpose === 'register' && user) throw ApiError.conflict('Số điện thoại đã được đăng ký');
  if (purpose !== 'register' && !user) throw ApiError.notFound('Số điện thoại chưa được đăng ký');
  if (user && user.status !== 'active') throw ApiError.forbidden('Tài khoản đã bị khoá');
  if (purpose === 'login' && !(await otpService.hasRecentLoginChallenge(phone))) {
    throw ApiError.badRequest('Phiên đăng nhập đã hết hạn, vui lòng nhập lại mật khẩu');
  }
  return otpService.send(phone, purpose);
}

/**
 * POST /auth/register/phone - màn hình OTP (luồng đăng ký) gọi sau khi người dùng nhập mã.
 * Tạo tài khoản + hồ sơ trống (chỉ có tên), trả token để app đi tiếp các màn hình onboarding.
 */
async function registerByPhone({ phone, otp, password, displayName }) {
  if (await findByPhone(phone)) throw ApiError.conflict('Số điện thoại đã được đăng ký');
  await otpService.verify(phone, 'register', otp);

  const passwordHash = await bcrypt.hash(password, 10);
  const user = await db.transaction(async (conn) => {
    const [result] = await conn.query(
      'INSERT INTO users (phone, phone_verified_at, password_hash) VALUES (?, UTC_TIMESTAMP(), ?)',
      [phone, passwordHash],
    );
    const userId = result.insertId;
    await conn.query('INSERT INTO profiles (user_id, display_name) VALUES (?, ?)', [userId, displayName]);
    await conn.query('INSERT INTO preferences (user_id) VALUES (?)', [userId]);
    return { id: userId, role: 'user' };
  });

  const tokens = await issueTokens(user);
  return { user: await usersService.getMe(user.id), tokens };
}

/**
 * POST /auth/login/phone - bước 1: kiểm tra SĐT + mật khẩu.
 * LOGIN_OTP_REQUIRED=true (mặc định, giống app): gửi OTP, trả { otpRequired: true, ... } -> app chuyển màn OTP.
 * LOGIN_OTP_REQUIRED=false: trả luôn token.
 */
async function loginByPhone({ phone, password }) {
  const user = await findByPhone(phone);
  const valid = user && (await bcrypt.compare(password, user.password_hash));
  if (!valid) throw ApiError.unauthorized('Số điện thoại hoặc mật khẩu không đúng');
  if (user.status !== 'active') throw ApiError.forbidden('Tài khoản đã bị khoá');

  if (env.loginOtpRequired) {
    return { otpRequired: true, ...(await otpService.send(phone, 'login')) };
  }
  return { otpRequired: false, user: await usersService.getMe(user.id), tokens: await issueTokens(user) };
}

/** POST /auth/login/phone/verify - bước 2: nhập OTP -> token. */
async function verifyLoginOtp({ phone, otp }) {
  const user = await findByPhone(phone);
  if (!user) throw ApiError.notFound('Số điện thoại chưa được đăng ký');
  if (user.status !== 'active') throw ApiError.forbidden('Tài khoản đã bị khoá');
  await otpService.verify(phone, 'login', otp);
  return { user: await usersService.getMe(user.id), tokens: await issueTokens(user) };
}

/**
 * POST /auth/otp/verify - màn hình OTP (luồng quên mật khẩu): đúng mã -> resetToken (10 phút)
 * để màn hình Đặt lại mật khẩu gửi kèm mật khẩu mới.
 */
async function verifyResetOtp({ phone, otp }) {
  const user = await findByPhone(phone);
  if (!user) throw ApiError.notFound('Số điện thoại chưa được đăng ký');
  await otpService.verify(phone, 'reset_password', otp);
  const resetToken = jwt.sign(
    { sub: String(user.id), pwf: passwordFingerprint(user.password_hash) },
    resetSecret(),
    { expiresIn: '10m' },
  );
  return { resetToken, expiresIn: 600 };
}

/** POST /auth/password/reset - đặt mật khẩu mới bằng resetToken (dùng 1 lần), đăng xuất mọi thiết bị. */
async function resetPassword({ resetToken, newPassword }) {
  let payload;
  try {
    payload = jwt.verify(resetToken, resetSecret());
  } catch {
    throw ApiError.badRequest('Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn, vui lòng thực hiện lại');
  }
  const [user] = await db.query('SELECT id, password_hash FROM users WHERE id = ?', [payload.sub]);
  // Mật khẩu đã đổi sau khi cấp token -> token đã được dùng
  if (!user || passwordFingerprint(user.password_hash) !== payload.pwf) {
    throw ApiError.badRequest('Phiên đặt lại mật khẩu đã được sử dụng, vui lòng thực hiện lại');
  }
  await db.query('UPDATE users SET password_hash = ? WHERE id = ?', [await bcrypt.hash(newPassword, 10), user.id]);
  await db.query('UPDATE refresh_tokens SET revoked_at = UTC_TIMESTAMP() WHERE user_id = ? AND revoked_at IS NULL', [user.id]);
}

// ═════════════════════ EMAIL (v1) ═════════════════════

async function login({ email, password }) {
  const [user] = await db.query('SELECT id, role, status, password_hash FROM users WHERE email = ?', [email]);
  const valid = user && (await bcrypt.compare(password, user.password_hash));
  if (!valid) throw ApiError.unauthorized('Email hoặc mật khẩu không đúng');
  if (user.status !== 'active') throw ApiError.forbidden('Tài khoản đã bị khoá');

  const tokens = await issueTokens(user);
  return { user: await usersService.getMe(user.id), tokens };
}

/** Đổi refresh token cũ lấy cặp token mới (refresh token xoay vòng, dùng 1 lần). */
async function refresh(refreshToken) {
  const hash = sha256(refreshToken);
  return db.transaction(async (conn) => {
    const [[row]] = await conn.query(
      `SELECT rt.id, rt.expires_at, rt.revoked_at, u.id AS user_id, u.role, u.status
         FROM refresh_tokens rt JOIN users u ON u.id = rt.user_id
        WHERE rt.token_hash = ? FOR UPDATE`,
      [hash],
    );
    if (!row || row.revoked_at || new Date(row.expires_at) < new Date()) {
      throw ApiError.unauthorized('Refresh token không hợp lệ hoặc đã hết hạn');
    }
    if (row.status !== 'active') throw ApiError.forbidden('Tài khoản đã bị khoá');

    await conn.query('UPDATE refresh_tokens SET revoked_at = UTC_TIMESTAMP() WHERE id = ?', [row.id]);
    return issueTokens({ id: row.user_id, role: row.role }, conn);
  });
}

async function logout(refreshToken) {
  await db.query('UPDATE refresh_tokens SET revoked_at = UTC_TIMESTAMP() WHERE token_hash = ? AND revoked_at IS NULL', [
    sha256(refreshToken),
  ]);
}

async function changePassword(userId, { currentPassword, newPassword }) {
  const [user] = await db.query('SELECT password_hash FROM users WHERE id = ?', [userId]);
  if (!user || !(await bcrypt.compare(currentPassword, user.password_hash))) {
    throw ApiError.badRequest('Mật khẩu hiện tại không đúng');
  }
  await db.query('UPDATE users SET password_hash = ? WHERE id = ?', [await bcrypt.hash(newPassword, 10), userId]);
  // Đăng xuất mọi thiết bị khác
  await db.query('UPDATE refresh_tokens SET revoked_at = UTC_TIMESTAMP() WHERE user_id = ? AND revoked_at IS NULL', [userId]);
}

module.exports = {
  register,
  login,
  sendOtp,
  registerByPhone,
  loginByPhone,
  verifyLoginOtp,
  verifyResetOtp,
  resetPassword,
  refresh,
  logout,
  changePassword,
  defaultPreferences,
};

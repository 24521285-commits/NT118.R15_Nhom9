/**
 * MÃ OTP 6 SỐ qua SMS cho 3 luồng của app: register, login, reset_password.
 *
 * - Mã lưu dạng băm SHA-256 (không lưu mã gốc), hết hạn sau OTP_TTL_SECONDS (mặc định 5 phút)
 * - Gửi lại sau OTP_RESEND_SECONDS (30 giây, khớp đồng hồ đếm ngược trên app)
 * - Tối đa OTP_MAX_PER_HOUR lần gửi / số / mục đích / giờ
 * - Sai quá 5 lần thì mã bị huỷ, phải gửi mã mới
 * - Gửi mã mới thì mã cũ (cùng mục đích) hết hiệu lực
 */
const crypto = require('crypto');
const env = require('../config/env');
const db = require('../config/db');
const ApiError = require('../utils/ApiError');
const sms = require('./sms');

const PURPOSE_TEXT = {
  register: 'đăng ký tài khoản',
  login: 'đăng nhập',
  reset_password: 'đặt lại mật khẩu',
};

const hashCode = (phone, purpose, code) => crypto.createHash('sha256').update(`${phone}:${purpose}:${code}`).digest('hex');

/** Tạo & gửi OTP. Trả về thông tin cho app (kèm devOtp khi bật OTP_DEV_MODE). */
async function send(phone, purpose) {
  const [last] = await db.query(
    `SELECT TIMESTAMPDIFF(SECOND, created_at, UTC_TIMESTAMP()) AS age
       FROM otp_codes WHERE phone = ? AND purpose = ? ORDER BY id DESC LIMIT 1`,
    [phone, purpose],
  );
  if (last && Number(last.age) < env.otp.resendSeconds) {
    const wait = env.otp.resendSeconds - Number(last.age);
    throw new ApiError(429, `Vui lòng đợi ${wait} giây trước khi gửi lại mã`, 'OTP_TOO_SOON', [{ field: 'retryAfter', message: String(wait) }]);
  }
  const [{ sent }] = await db.query(
    `SELECT COUNT(*) AS sent FROM otp_codes
      WHERE phone = ? AND purpose = ? AND created_at > UTC_TIMESTAMP() - INTERVAL 1 HOUR`,
    [phone, purpose],
  );
  if (Number(sent) >= env.otp.maxPerHour) {
    throw new ApiError(429, 'Bạn đã yêu cầu quá nhiều mã OTP, vui lòng thử lại sau 1 giờ', 'OTP_LIMIT');
  }

  const code = String(crypto.randomInt(0, 1000000)).padStart(6, '0');
  // Huỷ các mã cũ chưa dùng của cùng mục đích
  await db.query(
    'UPDATE otp_codes SET consumed_at = UTC_TIMESTAMP() WHERE phone = ? AND purpose = ? AND consumed_at IS NULL',
    [phone, purpose],
  );
  await db.query(
    `INSERT INTO otp_codes (phone, purpose, code_hash, expires_at)
     VALUES (?, ?, ?, UTC_TIMESTAMP() + INTERVAL ? SECOND)`,
    [phone, purpose, hashCode(phone, purpose, code), env.otp.ttlSeconds],
  );
  await sms.send(phone, `[DateZone] Ma xac thuc ${PURPOSE_TEXT[purpose]} cua ban la ${code}. Ma het han sau ${Math.round(env.otp.ttlSeconds / 60)} phut.`);

  return {
    phone: sms.maskPhone(phone),
    expiresIn: env.otp.ttlSeconds,
    resendAfter: env.otp.resendSeconds,
    ...(env.otp.devMode ? { devOtp: code } : {}),
  };
}

/** Kiểm tra OTP; đúng thì đánh dấu đã dùng (mỗi mã chỉ dùng 1 lần). Sai thì ném lỗi 400. */
async function verify(phone, purpose, code) {
  const [otp] = await db.query(
    `SELECT id, code_hash, attempts, (expires_at < UTC_TIMESTAMP()) AS expired
       FROM otp_codes WHERE phone = ? AND purpose = ? AND consumed_at IS NULL
      ORDER BY id DESC LIMIT 1`,
    [phone, purpose],
  );
  if (!otp) throw new ApiError(400, 'Mã OTP không tồn tại hoặc đã được sử dụng, vui lòng gửi lại mã', 'OTP_INVALID');
  if (Number(otp.expired)) throw new ApiError(400, 'Mã OTP đã hết hạn, vui lòng gửi lại mã', 'OTP_EXPIRED');

  const a = Buffer.from(otp.code_hash, 'hex');
  const b = Buffer.from(hashCode(phone, purpose, String(code)), 'hex');
  if (!crypto.timingSafeEqual(a, b)) {
    const attempts = Number(otp.attempts) + 1;
    const left = env.otp.maxAttempts - attempts;
    await db.query(
      `UPDATE otp_codes SET attempts = ?, consumed_at = IF(? <= 0, UTC_TIMESTAMP(), NULL) WHERE id = ?`,
      [attempts, left, otp.id],
    );
    if (left <= 0) throw new ApiError(400, 'Nhập sai quá nhiều lần, vui lòng gửi lại mã mới', 'OTP_LOCKED');
    throw new ApiError(400, `Mã OTP không đúng, còn ${left} lần thử`, 'OTP_WRONG');
  }
  await db.query('UPDATE otp_codes SET consumed_at = UTC_TIMESTAMP() WHERE id = ?', [otp.id]);
}

/** Đã có OTP đăng nhập được cấp trong 10 phút gần đây chưa (tức đã nhập đúng mật khẩu) -> cho phép gửi lại. */
async function hasRecentLoginChallenge(phone) {
  const rows = await db.query(
    `SELECT 1 FROM otp_codes WHERE phone = ? AND purpose = 'login'
        AND created_at > UTC_TIMESTAMP() - INTERVAL 10 MINUTE LIMIT 1`,
    [phone],
  );
  return rows.length > 0;
}

module.exports = { send, verify, hasRecentLoginChallenge };

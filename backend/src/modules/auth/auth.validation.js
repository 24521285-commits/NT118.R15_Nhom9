const { z } = require('zod');
const v = require('../../utils/validators');

const email = z.string().trim().toLowerCase().email('Email không hợp lệ').max(255);
const password = z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự').max(72, 'Mật khẩu tối đa 72 ký tự');

// ── Đăng ký / đăng nhập bằng EMAIL (bản v1 - giữ cho Postman, Web Admin, test nhanh) ──
const register = z
  .object({
    email,
    password,
    displayName: z.string().trim().min(1, 'Tên hiển thị không được trống').max(100),
    birthday: v.birthday,
    gender: v.option('gender'),
    // Không bắt buộc - dùng để tạo kết quả mặc định cho trang Home
    interestedIn: v.option('interestedIn').optional(),
    city: z.string().trim().max(100).optional(),
    latitude: z.number().min(-90).max(90).optional(),
    longitude: z.number().min(-180).max(180).optional(),
    interestIds: z.array(z.number().int().positive()).max(20, 'Tối đa 20 sở thích').optional(),
  })
  .refine((d) => (d.latitude === undefined) === (d.longitude === undefined), {
    message: 'Phải gửi cả latitude và longitude',
    path: ['latitude'],
  });

const login = z.object({
  email,
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
});

// ── SỐ ĐIỆN THOẠI + OTP (khớp app Android) ──
const otpSend = z.object({
  phone: v.phone,
  purpose: z.enum(['register', 'login', 'reset_password'], {
    errorMap: () => ({ message: 'purpose phải là register | login | reset_password' }),
  }),
});

const registerPhone = z.object({
  phone: v.phone,
  otp: v.otp,
  password: v.password8,
  displayName: v.displayName,
});

const loginPhone = z.object({
  phone: v.phone,
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
});

const loginPhoneVerify = z.object({
  phone: v.phone,
  otp: v.otp,
});

const otpVerifyReset = z.object({
  phone: v.phone,
  otp: v.otp,
});

const passwordReset = z.object({
  resetToken: z.string().min(1, 'Thiếu resetToken'),
  newPassword: v.password8,
});

const refresh = z.object({
  refreshToken: z.string().min(1, 'Thiếu refreshToken'),
});

const changePassword = z.object({
  currentPassword: z.string().min(1),
  newPassword: password,
});

module.exports = {
  register,
  login,
  otpSend,
  registerPhone,
  loginPhone,
  loginPhoneVerify,
  otpVerifyReset,
  passwordReset,
  refresh,
  changePassword,
};

const router = require('express').Router();
const h = require('../../utils/asyncHandler');
const validate = require('../../middlewares/validate');
const rateLimit = require('../../middlewares/rateLimit');
const { requireAuth } = require('../../middlewares/auth');
const schema = require('./auth.validation');
const ctrl = require('./auth.controller');

// Chống dò mật khẩu / spam SMS: tối đa N request / phút / IP
const loginLimit = rateLimit({ name: 'login', windowSec: 60, max: 20 });
const otpLimit = rateLimit({ name: 'otp', windowSec: 60, max: 5 });
const verifyLimit = rateLimit({ name: 'verify', windowSec: 60, max: 15 });

// ── Số điện thoại + OTP (app Android) ──
router.post('/otp/send', otpLimit, validate({ body: schema.otpSend }), h(ctrl.sendOtp));
router.post('/register/phone', verifyLimit, validate({ body: schema.registerPhone }), h(ctrl.registerPhone));
router.post('/login/phone', loginLimit, validate({ body: schema.loginPhone }), h(ctrl.loginPhone));
router.post('/login/phone/verify', verifyLimit, validate({ body: schema.loginPhoneVerify }), h(ctrl.loginPhoneVerify));
router.post('/otp/verify', verifyLimit, validate({ body: schema.otpVerifyReset }), h(ctrl.verifyResetOtp));
router.post('/password/reset', verifyLimit, validate({ body: schema.passwordReset }), h(ctrl.resetPassword));

// ── Email (v1) ──
router.post('/register', validate({ body: schema.register }), h(ctrl.register));
router.post('/login', loginLimit, validate({ body: schema.login }), h(ctrl.login));

router.post('/refresh', validate({ body: schema.refresh }), h(ctrl.refresh));
router.post('/logout', validate({ body: schema.refresh }), h(ctrl.logout));
router.put('/password', requireAuth, validate({ body: schema.changePassword }), h(ctrl.changePassword));

module.exports = router;

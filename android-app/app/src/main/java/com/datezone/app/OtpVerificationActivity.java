package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OtpVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_FLOW_TYPE = "EXTRA_FLOW_TYPE";
    public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

    public static final String FLOW_LOGIN = "FLOW_LOGIN";
    public static final String FLOW_REGISTER = "FLOW_REGISTER";
    public static final String FLOW_FORGOT_PASSWORD = "FLOW_FORGOT_PASSWORD";

    private EditText[] otpBoxes;
    private TextView tvOtpSubtitle;
    private TextView tvResendOtp;

    private String flowType = FLOW_FORGOT_PASSWORD;
    private String phoneNumber = "";
    private CountDownTimer countDownTimer;
    private boolean isResendClickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.lg_activity_otp_verification);

        View root = findViewById(R.id.otpRoot);
        if (root != null) {
            int left = root.getPaddingLeft();
            int top = root.getPaddingTop();
            int right = root.getPaddingRight();
            int bottom = root.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                                | WindowInsetsCompat.Type.ime()
                );

                view.setPadding(
                        left + bars.left,
                        top + bars.top,
                        right + bars.right,
                        bottom + bars.bottom
                );

                return insets;
            });
            ViewCompat.requestApplyInsets(root);
        }

        if (getIntent() != null) {
            if (getIntent().hasExtra(EXTRA_FLOW_TYPE)) {
                flowType = getIntent().getStringExtra(EXTRA_FLOW_TYPE);
            }
            if (getIntent().hasExtra(EXTRA_PHONE_NUMBER)) {
                phoneNumber = getIntent().getStringExtra(EXTRA_PHONE_NUMBER);
            }
        }

        tvOtpSubtitle = findViewById(R.id.tvOtpSubtitle);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        setupSubtitle();

        // Khởi tạo 6 ô OTP
        otpBoxes = new EditText[]{
                findViewById(R.id.edtOtp1),
                findViewById(R.id.edtOtp2),
                findViewById(R.id.edtOtp3),
                findViewById(R.id.edtOtp4),
                findViewById(R.id.edtOtp5),
                findViewById(R.id.edtOtp6)
        };

        setupOtpInputs();

        // Nút Back quay về trang trước
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());

        // Nút Xác nhận
        findViewById(R.id.btnVerify).setOnClickListener(view -> verifyOtpCode());

        // Text Gửi lại mã
        tvResendOtp.setOnClickListener(view -> {
            if (isResendClickable) {
                resendOtpCode();
            }
        });

        startResendTimer();
    }

    private void setupSubtitle() {
        String maskedPhone = maskPhoneNumber(phoneNumber);
        String subtitleText = "Nhập mã gồm 6 chữ số chúng tôi đã gửi tới\n" + maskedPhone;
        tvOtpSubtitle.setText(subtitleText);
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "+84 ••• ••• 123";
        }
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.length() >= 3) {
            String last3 = clean.substring(clean.length() - 3);
            return "+84 ••• ••• " + last3;
        }
        return "+84 ••• ••• 123";
    }

    private void setupOtpInputs() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;

            otpBoxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpBoxes.length - 1) {
                        otpBoxes[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpBoxes[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpBoxes[index].getText().toString().isEmpty() && index > 0) {
                        otpBoxes[index - 1].requestFocus();
                        otpBoxes[index - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private void verifyOtpCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) {
            sb.append(box.getText().toString().trim());
        }

        String otp = sb.toString();
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị dialog thông báo xác thực OTP thành công
        OtpSuccessDialog dialog = new OtpSuccessDialog(
                this,
                "Mã OTP của bạn đã được xác minh thành công.",
                () -> onOtpVerificationSuccess()
        );
        dialog.show();
    }

    private void onOtpVerificationSuccess() {
        if (FLOW_FORGOT_PASSWORD.equals(flowType)) {
            // Sau khi bấm xác nhận từ Quên mật khẩu -> Mới hiện ra trang Đặt lại mật khẩu
            Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
            intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
            startActivity(intent);
            finish();
        } else if (FLOW_LOGIN.equals(flowType)) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else if (FLOW_REGISTER.equals(flowType)) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OtpVerificationActivity.this, EnterNameActivity.class);
            intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    private void startResendTimer() {
        isResendClickable = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        int textSecondaryColor = androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary);
        int brandPrimaryColor = androidx.core.content.ContextCompat.getColor(this, R.color.brand_primary);

        tvResendOtp.setTextColor(textSecondaryColor);
        tvResendOtp.setTypeface(null, android.graphics.Typeface.NORMAL);

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvResendOtp.setText("Gửi lại mã sau " + seconds + "s");
            }

            @Override
            public void onFinish() {
                isResendClickable = true;
                tvResendOtp.setText("Gửi lại");
                tvResendOtp.setTextColor(brandPrimaryColor);
                tvResendOtp.setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }.start();
    }

    private void resendOtpCode() {
        Toast.makeText(this, "Đã gửi lại mã OTP mới!", Toast.LENGTH_SHORT).show();
        for (EditText box : otpBoxes) {
            box.setText("");
        }
        otpBoxes[0].requestFocus();
        startResendTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

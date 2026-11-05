package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.lg_activity_forgot_password);

        View root = findViewById(R.id.forgotPasswordRoot);
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

        edtPhone = findViewById(R.id.edtPhone);

        // Nút top-left Quay lại -> back về trang trước
        findViewById(R.id.btnBack).setOnClickListener(view -> finish());

        // Text "Quay lại đăng nhập" -> về trang đăng nhập
        findViewById(R.id.tvBackToLogin).setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Nút Gửi mã OTP
        findViewById(R.id.btnSendOtp).setOnClickListener(view -> checkInputAndSendOtp());
    }

    private void checkInputAndSendOtp() {
        String phone = edtPhone.getText().toString().trim()
                .replaceAll("[\\s().-]", "");

        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        }

        if (!phone.matches("^0[35789][0-9]{8}$")) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return;
        }

        String normalizedPhone = "+84" + phone.substring(1);

        // Chuyển sang màn hình Xác thực OTP với flow Quên mật khẩu
        Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_FLOW_TYPE, OtpVerificationActivity.FLOW_FORGOT_PASSWORD);
        intent.putExtra(OtpVerificationActivity.EXTRA_PHONE_NUMBER, normalizedPhone);
        startActivity(intent);
    }
}

package com.datezone.app;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName;
    private EditText edtPhone;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lg_activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        View root = findViewById(R.id.registerRoot);
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

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(view -> {
            checkRegisterInput();
        });

        // Đóng màn đăng ký để trở về đăng nhập.
        findViewById(R.id.tvLogin).setOnClickListener(view -> {
            finish();
        });
    }

    private void checkRegisterInput() {
        String fullName = edtFullName.getText().toString().trim();

        String phone = edtPhone.getText().toString().trim()
                .replaceAll("[\\s().-]", "");

        String password = edtPassword.getText().toString();
        String confirmPassword =
                edtConfirmPassword.getText().toString();

        if (fullName.isEmpty()) {
            showError(edtFullName, "Vui lòng nhập họ và tên");
            return;
        }

        if (phone.isEmpty()) {
            showError(edtPhone, "Vui lòng nhập số điện thoại");
            return;
        }

        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        }

        if (!phone.matches("^0[35789][0-9]{8}$")) {
            showError(edtPhone, "Số điện thoại không hợp lệ");
            return;
        }

        // Quy tắc tạm cho giao diện, cần thống nhất với backend.
        if (password.length() < 8) {
            showError(edtPassword, "Mật khẩu cần ít nhất 8 ký tự");
            return;
        }

        if (confirmPassword.isEmpty()) {
            showError(edtConfirmPassword, "Vui lòng nhập lại mật khẩu");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(edtConfirmPassword, "Mật khẩu nhập lại không khớp");
            return;
        }

        String normalizedPhone = "+84" + phone.substring(1);

        // Chuyển sang màn hình Xác thực OTP với flow Đăng ký
        android.content.Intent intent = new android.content.Intent(RegisterActivity.this, OtpVerificationActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_FLOW_TYPE, OtpVerificationActivity.FLOW_REGISTER);
        intent.putExtra(OtpVerificationActivity.EXTRA_PHONE_NUMBER, normalizedPhone);
        startActivity(intent);
    }

    private void showError(EditText input, String message) {
        input.setError(message);
        input.requestFocus();
    }
}
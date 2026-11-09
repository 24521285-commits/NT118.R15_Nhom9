package com.datezone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtCurrentPassword;
    private ImageView btnToggleVisibility;
    private TextView tvSubtitle;

    private boolean isPasswordVisible = false;
    private String userEmail = "minhanh.new@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_change_password);

        View root = findViewById(R.id.changePasswordRoot);
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

        edtCurrentPassword = findViewById(R.id.edtCurrentPasswordInput);
        btnToggleVisibility = findViewById(R.id.btnTogglePasswordVisibility);
        tvSubtitle = findViewById(R.id.tvChangePasswordSubtitle);

        SharedPreferences prefs = getSharedPreferences(ProfileEditActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String savedEmail = prefs.getString("KEY_SAVED_EMAIL", "minhanh.new@gmail.com");
        if (!TextUtils.isEmpty(savedEmail)) {
            userEmail = savedEmail;
        }

        if (tvSubtitle != null) {
            tvSubtitle.setText("Nhập mật khẩu hiện tại. Chúng tôi sẽ gửi mã OTP gồm 6 chữ số tới email " + userEmail + " để xác nhận.");
        }

        setupListeners();
    }

    private void setupListeners() {
        // Nút Back
        findViewById(R.id.btnChangePasswordBack).setOnClickListener(v -> finish());

        // An / Hiện mật khẩu
        if (btnToggleVisibility != null && edtCurrentPassword != null) {
            btnToggleVisibility.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible;
                if (isPasswordVisible) {
                    edtCurrentPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    edtCurrentPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                edtCurrentPassword.setSelection(edtCurrentPassword.getText().length());
            });
        }

        // Link Quên mật khẩu hiện tại? Đặt lại mật khẩu
        View btnForgot = findViewById(R.id.btnForgotPasswordLink);
        if (btnForgot != null) {
            btnForgot.setOnClickListener(v -> {
                Intent intent = new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            });
        }

        // Nút Gửi mã OTP
        View btnSendOtp = findViewById(R.id.btnSendOtpChangePassword);
        if (btnSendOtp != null) {
            btnSendOtp.setOnClickListener(v -> validateAndSendOtp());
        }
    }

    private void validateAndSendOtp() {
        String password = edtCurrentPassword != null ? edtCurrentPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đã gửi mã OTP gồm 6 chữ số tới " + userEmail, Toast.LENGTH_SHORT).show();

        // Mở màn hình nhập OTP lg_activity_otp_verification
        Intent intent = new Intent(ChangePasswordActivity.this, OtpVerificationActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_FLOW_TYPE, OtpVerificationActivity.FLOW_CHANGE_PASSWORD);
        intent.putExtra(OtpVerificationActivity.EXTRA_EMAIL, userEmail);
        startActivity(intent);
    }
}

package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangeEmailActivity extends AppCompatActivity {

    private EditText edtNewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_change_email);

        View root = findViewById(R.id.changeEmailRoot);
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

        edtNewEmail = findViewById(R.id.edtNewEmail);

        setupListeners();
    }

    private void setupListeners() {
        // Nút Back
        findViewById(R.id.btnChangeEmailBack).setOnClickListener(v -> finish());

        // Link Quay lại Cài đặt
        View btnReturn = findViewById(R.id.btnReturnToSettings);
        if (btnReturn != null) {
            btnReturn.setOnClickListener(v -> finish());
        }

        // Nút Gửi mã OTP
        View btnSend = findViewById(R.id.btnSendOtp);
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendOtpCode());
        }
    }

    private void sendOtpCode() {
        String newEmail = edtNewEmail != null ? edtNewEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newEmail) || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ email mới hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ChangeEmailActivity.this, ChangeEmailSuccessActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_EMAIL, newEmail);
        startActivity(intent);
        finish();
    }
}

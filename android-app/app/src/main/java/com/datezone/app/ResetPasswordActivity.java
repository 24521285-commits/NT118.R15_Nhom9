package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText edtNewPassword;
    private EditText edtConfirmPassword;

    private ImageView ivRuleMinLength;
    private TextView tvRuleMinLength;

    private ImageView ivRuleHasDigit;
    private TextView tvRuleHasDigit;

    private ImageView ivRuleMatch;
    private TextView tvRuleMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.lg_activity_reset_password);

        View root = findViewById(R.id.resetPasswordRoot);
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

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        ivRuleMinLength = findViewById(R.id.ivRuleMinLength);
        tvRuleMinLength = findViewById(R.id.tvRuleMinLength);

        ivRuleHasDigit = findViewById(R.id.ivRuleHasDigit);
        tvRuleHasDigit = findViewById(R.id.tvRuleHasDigit);

        ivRuleMatch = findViewById(R.id.ivRuleMatch);
        tvRuleMatch = findViewById(R.id.tvRuleMatch);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordRules();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtNewPassword.addTextChangedListener(textWatcher);
        edtConfirmPassword.addTextChangedListener(textWatcher);

        findViewById(R.id.btnResetPassword).setOnClickListener(view -> checkAndSubmit());
    }

    private boolean validatePasswordRules() {
        String newPassword = edtNewPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        boolean isMinLength = newPassword.length() >= 8;
        boolean isHasDigit = newPassword.matches(".*\\d.*");
        boolean isMatch = !newPassword.isEmpty() && newPassword.equals(confirmPassword);

        updateRuleState(isMinLength, ivRuleMinLength, tvRuleMinLength);
        updateRuleState(isHasDigit, ivRuleHasDigit, tvRuleHasDigit);
        updateRuleState(isMatch, ivRuleMatch, tvRuleMatch);

        return isMinLength && isHasDigit && isMatch;
    }

    private void updateRuleState(boolean isSatisfied, ImageView ivRule, TextView tvRule) {
        if (isSatisfied) {
            ivRule.setImageResource(R.drawable.ic_check_circle);
            tvRule.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            ivRule.setImageResource(R.drawable.ic_circle_outline);
            tvRule.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void checkAndSubmit() {
        if (!validatePasswordRules()) {
            Toast.makeText(
                    this,
                    "Vui lòng đảm bảo mật khẩu đáp ứng tất cả các yêu cầu",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Hiển thị Dialog thông báo Đặt lại mật khẩu thành công
        ResetPasswordSuccessDialog dialog = new ResetPasswordSuccessDialog(
                this,
                () -> {
                    // Chuyển sang màn hình chính Discovery (discovery_activity_home)
                    Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );
        dialog.show();
    }
}

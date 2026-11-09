package com.datezone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChangeEmailSuccessActivity extends AppCompatActivity {

    private String newEmail = "minhanh.new@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_change_email_success);

        View root = findViewById(R.id.changeEmailSuccessRoot);
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

        if (getIntent() != null && getIntent().hasExtra(OtpVerificationActivity.EXTRA_EMAIL)) {
            String custom = getIntent().getStringExtra(OtpVerificationActivity.EXTRA_EMAIL);
            if (custom != null && !custom.isEmpty()) {
                newEmail = custom;
            }
        }

        // Lưu email mới đã xác thực vào SharedPreferences
        SharedPreferences prefs = getSharedPreferences(ProfileEditActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("KEY_SAVED_EMAIL", newEmail).apply();

        TextView tvEmailNote = findViewById(R.id.tvSuccessEmailNote);
        if (tvEmailNote != null) {
            tvEmailNote.setText("Từ giờ bạn sẽ dùng\n" + newEmail + " để đăng nhập và nhận thông báo.");
        }

        View.OnClickListener returnToSettingsListener = v -> {
            Intent intent = new Intent(ChangeEmailSuccessActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        };

        findViewById(R.id.btnSuccessBack).setOnClickListener(returnToSettingsListener);

        View btnReturn = findViewById(R.id.btnReturnToSettingsScreen);
        if (btnReturn != null) {
            btnReturn.setOnClickListener(returnToSettingsListener);
        }
    }
}

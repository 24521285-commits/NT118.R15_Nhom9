package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReportUserActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "EXTRA_NAME";

    private String targetUserName = "Trương Vĩnh Nguyên";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.discovery_activity_report_user);

        View root = findViewById(R.id.reportUserRoot);
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

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_NAME)) {
            String name = intent.getStringExtra(EXTRA_NAME);
            if (name != null && !name.isEmpty()) {
                targetUserName = name;
            }
        }

        // Nút Back góc trên bên trái -> tái sử dụng nút back chuẩn
        findViewById(R.id.btnReportBack).setOnClickListener(v -> finish());

        // Nút Báo cáo và chặn
        findViewById(R.id.btnSubmitReport).setOnClickListener(v -> submitReportAndBlock());
    }

    private void submitReportAndBlock() {
        ReportSuccessDialog dialog = new ReportSuccessDialog(
                this,
                targetUserName,
                () -> {
                    Toast.makeText(this, "Đã gửi báo cáo và chặn thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển về màn hình chính HomeActivity và xóa sạch stack
                    Intent intent = new Intent(ReportUserActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );
        dialog.show();
    }
}

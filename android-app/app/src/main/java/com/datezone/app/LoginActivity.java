package com.datezone.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_login);

        // Giữ nội dung cách thanh trạng thái và thanh điều hướng.
        android.view.View root = findViewById(R.id.main);

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

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);

        findViewById(R.id.btnLogin).setOnClickListener(view -> {
            checkLoginInput();
        });

        findViewById(R.id.tvRegister).setOnClickListener(view -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(view -> {
            Toast.makeText(
                    this,
                    "Màn hình quên mật khẩu sẽ được làm sau",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void checkLoginInput() {
        String phone = edtPhone.getText().toString().trim()
                .replaceAll("[\\s().-]", "");

        String password = edtPassword.getText().toString();

        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            edtPhone.requestFocus();
            return;
        }

        // Chuyển đầu số +84 về 0 để kiểm tra.
        if (phone.startsWith("+84")) {
            phone = "0" + phone.substring(3);
        }

        // Kiểm tra định dạng số di động Việt Nam.
        if (!phone.matches("^0[35789][0-9]{8}$")) {
            edtPhone.setError("Số điện thoại không hợp lệ");
            edtPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return;
        }

        String normalizedPhone = "+84" + phone.substring(1);

        // Sẽ gửi normalizedPhone và password đến API đăng nhập.
        Toast.makeText(
                this,
                "Thông tin hợp lệ. Chưa kết nối API đăng nhập.",
                Toast.LENGTH_LONG
        ).show();
    }
}
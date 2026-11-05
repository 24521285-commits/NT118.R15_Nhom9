package com.datezone.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddPhotosActivity extends AppCompatActivity {

    private final View[] photoSlots = new View[6];
    private final Uri[] photoUris = new Uri[6];
    private int currentSelectingIndex = -1;

    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.lg2_activity_add_photos);

        View root = findViewById(R.id.addPhotosRoot);
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

        // Đăng ký Launcher chọn ảnh từ thư viện
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentSelectingIndex >= 0 && currentSelectingIndex < 6) {
                        setPhotoToSlot(currentSelectingIndex, uri);
                    }
                }
        );

        // Ánh xạ 6 ô ảnh
        photoSlots[0] = findViewById(R.id.photoSlot1);
        photoSlots[1] = findViewById(R.id.photoSlot2);
        photoSlots[2] = findViewById(R.id.photoSlot3);
        photoSlots[3] = findViewById(R.id.photoSlot4);
        photoSlots[4] = findViewById(R.id.photoSlot5);
        photoSlots[5] = findViewById(R.id.photoSlot6);

        for (int i = 0; i < photoSlots.length; i++) {
            final int index = i;
            if (photoSlots[i] != null) {
                // Click vào slot -> Mở thư viện ảnh
                photoSlots[i].setOnClickListener(v -> {
                    currentSelectingIndex = index;
                    photoPickerLauncher.launch("image/*");
                });

                // Click vào nút xóa ảnh
                View btnRemove = photoSlots[i].findViewById(R.id.btnRemovePhoto);
                if (btnRemove != null) {
                    btnRemove.setOnClickListener(v -> removePhotoFromSlot(index));
                }
            }
        }

        // Nút Quay lại
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút Tiếp tục dùng chung
        findViewById(R.id.btnContinue).setOnClickListener(v -> checkAndContinue());
    }

    private void setPhotoToSlot(int index, Uri uri) {
        photoUris[index] = uri;
        View slot = photoSlots[index];
        if (slot == null) return;

        ImageView ivPhoto = slot.findViewById(R.id.ivPhoto);
        View layoutPlaceholder = slot.findViewById(R.id.layoutPlaceholder);
        View btnRemovePhoto = slot.findViewById(R.id.btnRemovePhoto);

        if (ivPhoto != null) {
            ivPhoto.setImageURI(uri);
            ivPhoto.setVisibility(View.VISIBLE);
        }
        if (layoutPlaceholder != null) {
            layoutPlaceholder.setVisibility(View.GONE);
        }
        if (btnRemovePhoto != null) {
            btnRemovePhoto.setVisibility(View.VISIBLE);
        }
    }

    private void removePhotoFromSlot(int index) {
        photoUris[index] = null;
        View slot = photoSlots[index];
        if (slot == null) return;

        ImageView ivPhoto = slot.findViewById(R.id.ivPhoto);
        View layoutPlaceholder = slot.findViewById(R.id.layoutPlaceholder);
        View btnRemovePhoto = slot.findViewById(R.id.btnRemovePhoto);

        if (ivPhoto != null) {
            ivPhoto.setImageDrawable(null);
            ivPhoto.setVisibility(View.GONE);
        }
        if (layoutPlaceholder != null) {
            layoutPlaceholder.setVisibility(View.VISIBLE);
        }
        if (btnRemovePhoto != null) {
            btnRemovePhoto.setVisibility(View.GONE);
        }
    }

    private void checkAndContinue() {
        int uploadedCount = 0;
        for (Uri uri : photoUris) {
            if (uri != null) {
                uploadedCount++;
            }
        }

        if (uploadedCount < 2) {
            Toast.makeText(this, "Vui lòng tải lên ít nhất 2 ảnh để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang lg3_activity_birthday
        Intent intent = new Intent(AddPhotosActivity.this, BirthdayActivity.class);
        startActivity(intent);
    }
}

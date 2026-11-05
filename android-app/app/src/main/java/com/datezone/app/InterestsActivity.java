package com.datezone.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class InterestsActivity extends AppCompatActivity {

    private TextView tvInterestCount;
    private ChipGroup chipGroupInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.lg9_activity_interests);

        View root = findViewById(R.id.interestsRoot);
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

        tvInterestCount = findViewById(R.id.tvInterestCount);
        chipGroupInterests = findViewById(R.id.chipGroupInterests);

        chipGroupInterests.clearCheck();
        setupChipListeners();
        updateSelectedCount();

        // Nút Quay lại góc trên bên trái -> quay về lg8_activity_weight
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Nút Tiếp tục dùng chung
        findViewById(R.id.btnContinue).setOnClickListener(v -> checkAndContinue());
    }

    private void setupChipListeners() {
        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            View child = chipGroupInterests.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> updateSelectedCount());
            }
        }
    }

    private void updateSelectedCount() {
        int count = getSelectedInterests().size();
        tvInterestCount.setText("Chọn ít nhất 3 sở thích · Đã chọn " + count);
    }

    private List<String> getSelectedInterests() {
        List<String> selectedList = new ArrayList<>();
        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            View child = chipGroupInterests.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selectedList.add(chip.getText().toString());
                }
            }
        }
        return selectedList;
    }

    private void checkAndContinue() {
        List<String> selected = getSelectedInterests();

        if (selected.size() < 3) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 3 sở thích", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chuyển sang lg10_activity_relationship_goal
        android.content.Intent intent = new android.content.Intent(InterestsActivity.this, RelationshipGoalActivity.class);
        intent.putStringArrayListExtra("EXTRA_INTERESTS", new java.util.ArrayList<>(selected));
        startActivity(intent);
    }
}

package com.datezone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ProfileEditActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "UserProfilePrefs";
    public static final String KEY_NAME = "KEY_NAME";
    public static final String KEY_BIO = "KEY_BIO";
    public static final String KEY_GENDER = "KEY_GENDER";
    public static final String KEY_HEIGHT = "KEY_HEIGHT";
    public static final String KEY_WEIGHT = "KEY_WEIGHT";
    public static final String KEY_LOCATION = "KEY_LOCATION";
    public static final String KEY_RELATIONSHIP = "KEY_RELATIONSHIP";
    public static final String KEY_ZODIAC = "KEY_ZODIAC";
    public static final String KEY_SMOKING = "KEY_SMOKING";
    public static final String KEY_DRINKING = "KEY_DRINKING";
    public static final String KEY_EXERCISE = "KEY_EXERCISE";
    public static final String KEY_EDUCATION = "KEY_EDUCATION";

    private GridLayout gridPhotos;
    private TextView tvPhotoCount;
    private final List<Object> photosList = new ArrayList<>();

    private ChipGroup chipGroupSelectedInterests;
    private final ArrayList<String> selectedInterestsList = new ArrayList<>();

    private EditText edtName;
    private EditText edtBio;
    private EditText edtGender;
    private EditText edtHeight;
    private EditText edtWeight;
    private EditText edtLocation;

    private TextView tvRelationship;
    private TextView tvZodiac;
    private TextView tvSmoking;
    private TextView tvDrinking;
    private TextView tvExercise;
    private TextView tvEducation;

    private ActivityResultLauncher<String> photoPickerLauncher;
    private ActivityResultLauncher<Intent> interestsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_edit);

        View root = findViewById(R.id.editProfileRoot);
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

        // Khởi tạo launcher chọn ảnh từ thiết bị
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && photosList.size() < 6) {
                        photosList.add(uri);
                        renderPhotosGrid();
                    }
                }
        );

        // Khởi tạo launcher mở trang Sở thích
        interestsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> updatedInterests = result.getData().getStringArrayListExtra(InterestsActivity.EXTRA_SELECTED_INTERESTS);
                        if (updatedInterests != null) {
                            selectedInterestsList.clear();
                            selectedInterestsList.addAll(updatedInterests);
                            renderSelectedInterests();
                        }
                    }
                }
        );

        initViews();
        loadInitialPhotosAndInterests();
        loadSavedData();
        setupListeners();
    }

    private void initViews() {
        gridPhotos = findViewById(R.id.gridEditProfilePhotos);
        tvPhotoCount = findViewById(R.id.tvEditProfilePhotoCount);

        chipGroupSelectedInterests = findViewById(R.id.chipGroupSelectedInterests);

        edtName = findViewById(R.id.edtEditProfileName);
        edtBio = findViewById(R.id.edtEditProfileBio);
        edtGender = findViewById(R.id.edtEditProfileGender);
        edtHeight = findViewById(R.id.edtEditProfileHeight);
        edtWeight = findViewById(R.id.edtEditProfileWeight);
        edtLocation = findViewById(R.id.edtEditProfileLocation);

        tvRelationship = findViewById(R.id.tvEditProfileRelationship);
        tvZodiac = findViewById(R.id.tvEditProfileZodiac);
        tvSmoking = findViewById(R.id.tvEditProfileSmoking);
        tvDrinking = findViewById(R.id.tvEditProfileDrinking);
        tvExercise = findViewById(R.id.tvEditProfileExercise);
        tvEducation = findViewById(R.id.tvEditProfileEducation);
    }

    private void loadInitialPhotosAndInterests() {
        // Khởi tạo 2 ảnh mặc định
        photosList.clear();
        photosList.add(R.drawable.discovery_photo_1);
        photosList.add(R.drawable.discovery_photo_2);
        renderPhotosGrid();

        // Khởi tạo các sở thích mặc định
        selectedInterestsList.clear();
        selectedInterestsList.add("Du lịch");
        selectedInterestsList.add("Âm nhạc");
        selectedInterestsList.add("Cà phê");
        selectedInterestsList.add("Ẩm thực");
        renderSelectedInterests();
    }

    private void renderPhotosGrid() {
        if (gridPhotos == null) return;
        gridPhotos.removeAllViews();

        float density = getResources().getDisplayMetrics().density;
        int slotWidthPx = (int) (100 * density);
        int slotHeightPx = (int) (132 * density);
        int marginPx = (int) (6 * density);
        int brandPrimaryColor = ContextCompat.getColor(this, R.color.brand_primary);

        if (tvPhotoCount != null) {
            tvPhotoCount.setText(photosList.size() + "/6 ảnh");
        }

        for (int i = 0; i < 6; i++) {
            final int index = i;

            CardView cardView = new CardView(this);
            GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
            cardParams.width = slotWidthPx;
            cardParams.height = slotHeightPx;
            cardParams.setMargins(marginPx, marginPx, marginPx, marginPx);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(14 * density);
            cardView.setCardElevation(0);
            cardView.setPreventCornerOverlap(false);

            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            if (i < photosList.size()) {
                // Ô có ảnh
                ImageView ivPhoto = new ImageView(this);
                ivPhoto.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));
                ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Object photoObj = photosList.get(i);
                if (photoObj instanceof Integer) {
                    ivPhoto.setImageResource((Integer) photoObj);
                } else if (photoObj instanceof Uri) {
                    try {
                        ivPhoto.setImageURI((Uri) photoObj);
                    } catch (Exception e) {
                        ivPhoto.setImageResource(R.drawable.discovery_photo_1);
                    }
                }

                // Nút 'X' xóa ảnh ở góc trên bên phải
                ImageView btnRemove = new ImageView(this);
                int btnSizePx = (int) (24 * density);
                FrameLayout.LayoutParams removeParams = new FrameLayout.LayoutParams(btnSizePx, btnSizePx);
                removeParams.gravity = Gravity.TOP | Gravity.END;
                removeParams.setMargins(0, (int) (4 * density), (int) (4 * density), 0);
                btnRemove.setLayoutParams(removeParams);
                btnRemove.setBackgroundResource(R.drawable.bg_circle_add);
                btnRemove.getBackground().mutate().setTint(Color.parseColor("#80000000"));
                btnRemove.setPadding((int) (5 * density), (int) (5 * density), (int) (5 * density), (int) (5 * density));
                btnRemove.setImageResource(R.drawable.ic_remove);
                btnRemove.setColorFilter(Color.WHITE);

                btnRemove.setOnClickListener(v -> {
                    photosList.remove(index);
                    renderPhotosGrid();
                });

                frameLayout.addView(ivPhoto);
                frameLayout.addView(btnRemove);

                // Thẻ 'Ảnh chính' ở góc dưới bên trái của ảnh đầu tiên
                if (i == 0) {
                    TextView tvMainBadge = new TextView(this);
                    FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    badgeParams.gravity = Gravity.BOTTOM | Gravity.START;
                    badgeParams.setMargins((int) (6 * density), 0, 0, (int) (6 * density));
                    tvMainBadge.setLayoutParams(badgeParams);
                    tvMainBadge.setBackgroundResource(R.drawable.bg_circle_add);
                    tvMainBadge.setPadding((int) (8 * density), (int) (3 * density), (int) (8 * density), (int) (3 * density));
                    tvMainBadge.setText("Ảnh chính");
                    tvMainBadge.setTextColor(Color.WHITE);
                    tvMainBadge.setTextSize(10f);
                    tvMainBadge.setTypeface(null, android.graphics.Typeface.BOLD);

                    frameLayout.addView(tvMainBadge);
                }
            } else {
                // Ô trống '+' viền nét đứt
                cardView.setCardBackgroundColor(Color.parseColor("#F8FAFC"));
                cardView.setBackgroundResource(R.drawable.bg_photo_slot);

                ImageView ivPlus = new ImageView(this);
                int plusSizePx = (int) (26 * density);
                FrameLayout.LayoutParams plusParams = new FrameLayout.LayoutParams(plusSizePx, plusSizePx);
                plusParams.gravity = Gravity.CENTER;
                ivPlus.setLayoutParams(plusParams);
                ivPlus.setImageResource(R.drawable.ic_add);
                ivPlus.setColorFilter(brandPrimaryColor);

                frameLayout.addView(ivPlus);

                if (i == photosList.size()) {
                    // Ô '+' kế tiếp cho phép bấm chọn thêm ảnh mới từ thiết bị
                    cardView.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
                } else {
                    cardView.setOnClickListener(v -> Toast.makeText(this, "Vui lòng thêm ảnh theo thứ tự", Toast.LENGTH_SHORT).show());
                }
            }

            cardView.addView(frameLayout);
            gridPhotos.addView(cardView);
        }
    }

    private void renderSelectedInterests() {
        if (chipGroupSelectedInterests == null) return;
        chipGroupSelectedInterests.removeAllViews();

        int brandPrimaryColor = ContextCompat.getColor(this, R.color.brand_primary);

        for (String interest : selectedInterestsList) {
            Chip chip = new Chip(this);
            chip.setText(interest);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setTextColor(Color.WHITE);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(brandPrimaryColor));
            chipGroupSelectedInterests.addView(chip);
        }
    }

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String savedName = prefs.getString(KEY_NAME, "Trương Vĩnh Nguyên");
        String savedBio = prefs.getString(KEY_BIO, "Thích du lịch, cà phê và nghe nhạc indie.");
        String savedGender = prefs.getString(KEY_GENDER, "Nam");
        String savedHeight = prefs.getString(KEY_HEIGHT, "170 cm");
        String savedWeight = prefs.getString(KEY_WEIGHT, "65 kg");
        String savedLocation = prefs.getString(KEY_LOCATION, "Hồ Chí Minh, Việt Nam");

        String savedRelationship = prefs.getString(KEY_RELATIONSHIP, "Người yêu lâu dài");
        String savedZodiac = prefs.getString(KEY_ZODIAC, "Bảo Bình");
        String savedSmoking = prefs.getString(KEY_SMOKING, "Không bao giờ");
        String savedDrinking = prefs.getString(KEY_DRINKING, "Thỉnh thoảng");
        String savedExercise = prefs.getString(KEY_EXERCISE, "Mỗi ngày");
        String savedEducation = prefs.getString(KEY_EDUCATION, "Đại học");

        if (edtName != null) edtName.setText(savedName);
        if (edtBio != null) edtBio.setText(savedBio);
        if (edtGender != null) edtGender.setText(savedGender);
        if (edtHeight != null) edtHeight.setText(savedHeight);
        if (edtWeight != null) edtWeight.setText(savedWeight);
        if (edtLocation != null) edtLocation.setText(savedLocation);

        setOptionText(tvRelationship, savedRelationship);
        setOptionText(tvZodiac, savedZodiac);
        setOptionText(tvSmoking, savedSmoking);
        setOptionText(tvDrinking, savedDrinking);
        setOptionText(tvExercise, savedExercise);
        setOptionText(tvEducation, savedEducation);

        syncChipSelections(findViewById(R.id.chipGroupEditProfileRelationship), savedRelationship);
        syncChipSelections(findViewById(R.id.chipGroupEditProfileZodiac), savedZodiac);
        syncChipSelections(findViewById(R.id.chipGroupEditProfileSmoking), savedSmoking);
        syncChipSelections(findViewById(R.id.chipGroupEditProfileDrinking), savedDrinking);
        syncChipSelections(findViewById(R.id.chipGroupEditProfileExercise), savedExercise);
        syncChipSelections(findViewById(R.id.chipGroupEditProfileEducation), savedEducation);
    }

    private void setOptionText(TextView textView, String value) {
        if (textView != null) {
            textView.setText(value);
            textView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnEditProfileBack).setOnClickListener(v -> finish());

        View btnSaveHeader = findViewById(R.id.btnEditProfileSave);
        if (btnSaveHeader != null) btnSaveHeader.setOnClickListener(v -> saveProfileData());

        View btnSaveBottom = findViewById(R.id.btnEditProfileSaveChanges);
        if (btnSaveBottom != null) btnSaveBottom.setOnClickListener(v -> saveProfileData());

        // Nút Chỉnh sửa Sở thích
        View btnEditInterests = findViewById(R.id.btnEditProfileInterests);
        if (btnEditInterests != null) {
            btnEditInterests.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileEditActivity.this, InterestsActivity.class);
                intent.putStringArrayListExtra(InterestsActivity.EXTRA_SELECTED_INTERESTS, selectedInterestsList);
                intent.putExtra(InterestsActivity.EXTRA_IS_EDIT, true);
                interestsLauncher.launch(intent);
            });
        }

        // Mở rộng / thu gọn các mục Mối quan hệ & Lối sống chuẩn như FiltersActivity
        setupExpandableLifestyleSections();
    }

    private void setupExpandableLifestyleSections() {
        setupExpandableRow(
                findViewById(R.id.rowEditProfileRelationship),
                findViewById(R.id.containerEditProfileRelationshipExpanded),
                findViewById(R.id.ivArrowEditProfileRelationship),
                findViewById(R.id.chipGroupEditProfileRelationship),
                tvRelationship
        );

        setupExpandableRow(
                findViewById(R.id.rowEditProfileZodiac),
                findViewById(R.id.containerEditProfileZodiacExpanded),
                findViewById(R.id.ivArrowEditProfileZodiac),
                findViewById(R.id.chipGroupEditProfileZodiac),
                tvZodiac
        );

        setupExpandableRow(
                findViewById(R.id.rowEditProfileSmoking),
                findViewById(R.id.containerEditProfileSmokingExpanded),
                findViewById(R.id.ivArrowEditProfileSmoking),
                findViewById(R.id.chipGroupEditProfileSmoking),
                tvSmoking
        );

        setupExpandableRow(
                findViewById(R.id.rowEditProfileDrinking),
                findViewById(R.id.containerEditProfileDrinkingExpanded),
                findViewById(R.id.ivArrowEditProfileDrinking),
                findViewById(R.id.chipGroupEditProfileDrinking),
                tvDrinking
        );

        setupExpandableRow(
                findViewById(R.id.rowEditProfileExercise),
                findViewById(R.id.containerEditProfileExerciseExpanded),
                findViewById(R.id.ivArrowEditProfileExercise),
                findViewById(R.id.chipGroupEditProfileExercise),
                tvExercise
        );

        setupExpandableRow(
                findViewById(R.id.rowEditProfileEducation),
                findViewById(R.id.containerEditProfileEducationExpanded),
                findViewById(R.id.ivArrowEditProfileEducation),
                findViewById(R.id.chipGroupEditProfileEducation),
                tvEducation
        );
    }

    private void setupExpandableRow(View rowView, LinearLayout container, ImageView arrow, ChipGroup chipGroup, TextView targetTextView) {
        if (rowView != null && container != null) {
            rowView.setOnClickListener(v -> toggleSection(container, arrow));
        }

        if (chipGroup != null && targetTextView != null) {
            for (int i = 0; i < chipGroup.getChildCount(); i++) {
                View child = chipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip chip = (Chip) child;
                    chip.setOnClickListener(v -> {
                        if (chip.isChecked()) {
                            setOptionText(targetTextView, chip.getText().toString());
                        }
                    });
                }
            }
        }
    }

    private void toggleSection(LinearLayout container, ImageView arrow) {
        if (container == null) return;

        if (container.getVisibility() == View.GONE) {
            container.setVisibility(View.VISIBLE);
            if (arrow != null) arrow.animate().rotation(180f).setDuration(200).start();
        } else {
            container.setVisibility(View.GONE);
            if (arrow != null) arrow.animate().rotation(0f).setDuration(200).start();
        }
    }

    private void syncChipSelections(ChipGroup chipGroup, String savedValue) {
        if (chipGroup == null || savedValue == null) return;
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (savedValue.equalsIgnoreCase(chip.getText().toString())) {
                    chip.setChecked(true);
                }
            }
        }
    }

    private void saveProfileData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (edtName != null) editor.putString(KEY_NAME, edtName.getText().toString().trim());
        if (edtBio != null) editor.putString(KEY_BIO, edtBio.getText().toString().trim());
        if (edtGender != null) editor.putString(KEY_GENDER, edtGender.getText().toString().trim());
        if (edtHeight != null) editor.putString(KEY_HEIGHT, edtHeight.getText().toString().trim());
        if (edtWeight != null) editor.putString(KEY_WEIGHT, edtWeight.getText().toString().trim());
        if (edtLocation != null) editor.putString(KEY_LOCATION, edtLocation.getText().toString().trim());

        if (tvRelationship != null) editor.putString(KEY_RELATIONSHIP, tvRelationship.getText().toString());
        if (tvZodiac != null) editor.putString(KEY_ZODIAC, tvZodiac.getText().toString());
        if (tvSmoking != null) editor.putString(KEY_SMOKING, tvSmoking.getText().toString());
        if (tvDrinking != null) editor.putString(KEY_DRINKING, tvDrinking.getText().toString());
        if (tvExercise != null) editor.putString(KEY_EXERCISE, tvExercise.getText().toString());
        if (tvEducation != null) editor.putString(KEY_EDUCATION, tvEducation.getText().toString());

        editor.apply();

        Toast.makeText(this, "Đã cập nhật hồ sơ cá nhân thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}

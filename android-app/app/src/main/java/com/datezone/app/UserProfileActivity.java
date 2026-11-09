package com.datezone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView ivProfileCover;
    private TextView tvProfileNameAge;
    private TextView tvProfileGender;
    private TextView tvProfileLocation;
    private TextView tvProfileBio;

    private TextView tvProfileGoal;
    private TextView tvProfileZodiac;
    private TextView tvProfileSmoking;
    private TextView tvProfileDrinking;

    private TextView tvProfileExercise;
    private TextView tvProfileEducation;

    private ChipGroup chipGroupProfileInterests;

    private int[] userPhotoResIds = new int[]{
            R.drawable.discovery_photo_1,
            R.drawable.discovery_photo_2,
            R.drawable.discovery_photo_3,
            R.drawable.discovery_photo_4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_user);

        View root = findViewById(R.id.userProfileRoot);
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

        initViews();
        loadSavedUserData();
        setupListeners();
    }

    private void initViews() {
        ivProfileCover = findViewById(R.id.ivProfileCover);
        tvProfileNameAge = findViewById(R.id.tvProfileNameAge);
        tvProfileGender = findViewById(R.id.tvProfileGender);
        tvProfileLocation = findViewById(R.id.tvProfileLocation);
        tvProfileBio = findViewById(R.id.tvProfileBio);

        tvProfileGoal = findViewById(R.id.tvProfileGoal);
        tvProfileZodiac = findViewById(R.id.tvProfileZodiac);
        tvProfileSmoking = findViewById(R.id.tvProfileSmoking);
        tvProfileDrinking = findViewById(R.id.tvProfileDrinking);
        tvProfileExercise = findViewById(R.id.tvProfileExercise);
        tvProfileEducation = findViewById(R.id.tvProfileEducation);

        chipGroupProfileInterests = findViewById(R.id.chipGroupProfileInterests);
    }

    private void loadSavedUserData() {
        SharedPreferences prefs = getSharedPreferences(ProfileEditActivity.PREFS_NAME, Context.MODE_PRIVATE);

        String name = prefs.getString(ProfileEditActivity.KEY_NAME, "Trương Vĩnh Nguyên");
        String bio = prefs.getString(ProfileEditActivity.KEY_BIO, "Thích du lịch, cà phê và trải nghiệm các địa điểm mới.");
        String gender = prefs.getString(ProfileEditActivity.KEY_GENDER, "Nam");
        String height = prefs.getString(ProfileEditActivity.KEY_HEIGHT, "170 cm");
        String weight = prefs.getString(ProfileEditActivity.KEY_WEIGHT, "65 kg");
        String location = prefs.getString(ProfileEditActivity.KEY_LOCATION, "Thành phố Hồ Chí Minh, Việt Nam");

        String relationship = prefs.getString(ProfileEditActivity.KEY_RELATIONSHIP, "Người yêu lâu dài");
        String zodiac = prefs.getString(ProfileEditActivity.KEY_ZODIAC, "Bảo Bình");
        String smoking = prefs.getString(ProfileEditActivity.KEY_SMOKING, "Không bao giờ");
        String drinking = prefs.getString(ProfileEditActivity.KEY_DRINKING, "Thỉnh thoảng");
        String exercise = prefs.getString(ProfileEditActivity.KEY_EXERCISE, "Mỗi ngày");
        String education = prefs.getString(ProfileEditActivity.KEY_EDUCATION, "Đại học");

        if (tvProfileNameAge != null) tvProfileNameAge.setText(name + ", 20");
        if (tvProfileGender != null) tvProfileGender.setText("Chiều cao " + height + "  Cân nặng " + weight + "  Giới tính " + gender);
        if (tvProfileLocation != null) tvProfileLocation.setText(location);
        if (tvProfileBio != null) tvProfileBio.setText(bio);

        if (tvProfileGoal != null) tvProfileGoal.setText("Mục tiêu: " + relationship);
        if (tvProfileZodiac != null) tvProfileZodiac.setText("Cung hoàng đạo: " + zodiac);
        if (tvProfileSmoking != null) tvProfileSmoking.setText("Hút thuốc: " + smoking);
        if (tvProfileDrinking != null) tvProfileDrinking.setText("Rượu bia: " + drinking);
        if (tvProfileExercise != null) tvProfileExercise.setText("Tập luyện: " + exercise);
        if (tvProfileEducation != null) tvProfileEducation.setText("Học vấn: " + education);

        // Nạp Chip sở thích
        if (chipGroupProfileInterests != null) {
            chipGroupProfileInterests.removeAllViews();
            int brandPrimaryColor = ContextCompat.getColor(this, R.color.brand_primary);
            String[] defaultInterests = new String[]{"Du lịch", "Âm nhạc", "Cà phê", "Ẩm thực"};

            for (String interest : defaultInterests) {
                Chip chip = new Chip(this);
                chip.setText(interest);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setTextColor(Color.WHITE);
                chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(brandPrimaryColor));
                chipGroupProfileInterests.addView(chip);
            }
        }
    }

    private void setupListeners() {
        // Quay về ProfileHomeActivity khi bấm Back
        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());

        // Bấm xem tất cả ảnh / chọn ảnh album -> Mở PhotoViewerActivity
        View.OnClickListener photoViewerListener = v -> {
            Intent intent = new Intent(UserProfileActivity.this, PhotoViewerActivity.class);
            intent.putExtra(PhotoViewerActivity.EXTRA_PHOTOS, userPhotoResIds);
            startActivity(intent);
        };

        View btnViewAllPhotos = findViewById(R.id.btnProfileViewAllPhotos);
        if (btnViewAllPhotos != null) btnViewAllPhotos.setOnClickListener(photoViewerListener);

        View galleryCard1 = findViewById(R.id.profileGalleryCard1);
        if (galleryCard1 != null) galleryCard1.setOnClickListener(photoViewerListener);

        View galleryCard2 = findViewById(R.id.profileGalleryCard2);
        if (galleryCard2 != null) galleryCard2.setOnClickListener(photoViewerListener);

        View galleryCard3 = findViewById(R.id.profileGalleryCard3);
        if (galleryCard3 != null) galleryCard3.setOnClickListener(photoViewerListener);

        View galleryCard4 = findViewById(R.id.profileGalleryCard4);
        if (galleryCard4 != null) galleryCard4.setOnClickListener(photoViewerListener);
    }
}

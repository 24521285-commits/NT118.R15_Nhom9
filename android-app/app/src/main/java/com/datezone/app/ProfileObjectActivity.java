package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class ProfileObjectActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_AGE = "EXTRA_AGE";
    public static final String EXTRA_DETAILS = "EXTRA_DETAILS";
    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";
    public static final String EXTRA_BIO = "EXTRA_BIO";
    public static final String EXTRA_INTERESTS = "EXTRA_INTERESTS";
    public static final String EXTRA_PHOTOS = "EXTRA_PHOTOS";

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

    private ImageView ivGallery1, ivGallery2, ivGallery3, ivGallery4;

    private String currentUserName = "Trương Vĩnh Nguyên";
    private int[] photoResIds = new int[]{R.drawable.discovery_photo_1, R.drawable.discovery_photo_2, R.drawable.discovery_photo_3, R.drawable.discovery_photo_4, R.drawable.discovery_photo_5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.discovery_activity_profile_object);

        View root = findViewById(R.id.profileDetailRoot);
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
        bindDataFromIntent();
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

        ivGallery1 = findViewById(R.id.ivProfileGalleryPhoto1);
        ivGallery2 = findViewById(R.id.ivProfileGalleryPhoto2);
        ivGallery3 = findViewById(R.id.ivProfileGalleryPhoto3);
        ivGallery4 = findViewById(R.id.ivProfileGalleryPhoto4);
    }

    private void bindDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.hasExtra(EXTRA_NAME) ? intent.getStringExtra(EXTRA_NAME) : "Trương Vĩnh Nguyên";
            String age = intent.hasExtra(EXTRA_AGE) ? intent.getStringExtra(EXTRA_AGE) : "20 tuổi";
            String details = intent.hasExtra(EXTRA_DETAILS) ? intent.getStringExtra(EXTRA_DETAILS) : "Chiều cao 170 cm  Cân nặng 65 kg  Giới tính Nam";
            String location = intent.hasExtra(EXTRA_LOCATION) ? intent.getStringExtra(EXTRA_LOCATION) : "Thành phố Hồ Chí Minh  Cách bạn 1,2 km";
            String bio = intent.hasExtra(EXTRA_BIO) ? intent.getStringExtra(EXTRA_BIO) : "Thích du lịch, cà phê và trải nghiệm các địa điểm mới. Mong muốn kết nối với những người bạn tích cực và chân thành.";

            if (name != null && !name.isEmpty()) {
                currentUserName = name;
            }

            if (intent.hasExtra(EXTRA_PHOTOS)) {
                int[] customPhotos = intent.getIntArrayExtra(EXTRA_PHOTOS);
                if (customPhotos != null && customPhotos.length > 0) {
                    photoResIds = customPhotos;
                }
            }

            tvProfileNameAge.setText(currentUserName + ", " + age);
            tvProfileGender.setText(details);
            tvProfileLocation.setText(location);
            tvProfileBio.setText(bio);

            if (photoResIds.length > 0) ivProfileCover.setImageResource(photoResIds[0]);
            if (photoResIds.length > 1) ivGallery1.setImageResource(photoResIds[1]);
            if (photoResIds.length > 2) ivGallery2.setImageResource(photoResIds[2]);
            if (photoResIds.length > 3) ivGallery3.setImageResource(photoResIds[3]);
            if (photoResIds.length > 4) ivGallery4.setImageResource(photoResIds[4]);

            ArrayList<String> interests = intent.getStringArrayListExtra(EXTRA_INTERESTS);
            if (interests == null || interests.isEmpty()) {
                interests = new ArrayList<>();
                interests.add("Du lịch");
                interests.add("Âm nhạc");
                interests.add("Cà phê");
                interests.add("Ẩm thực");
            }
            populateInterestsChips(interests);
        }
    }

    private void populateInterestsChips(ArrayList<String> interests) {
        if (chipGroupProfileInterests == null) return;
        chipGroupProfileInterests.removeAllViews();

        for (String interestText : interests) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_profile_interest_chip, chipGroupProfileInterests, false);
            chip.setText(interestText);
            chipGroupProfileInterests.addView(chip);
        }
    }

    private void setupListeners() {
        // Nút quay lại góc trên bên trái -> quay về màn hình trước (Home)
        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());

        // Bấm Xem tất cả ảnh -> Mở PhotoViewerActivity
        View.OnClickListener openPhotoViewerListener = v -> {
            Intent intent = new Intent(ProfileObjectActivity.this, PhotoViewerActivity.class);
            intent.putExtra(PhotoViewerActivity.EXTRA_PHOTOS, photoResIds);
            startActivity(intent);
        };

        findViewById(R.id.btnProfileViewAllPhotos).setOnClickListener(openPhotoViewerListener);
        findViewById(R.id.profilePhotoGallery).setOnClickListener(openPhotoViewerListener);
        ivProfileCover.setOnClickListener(openPhotoViewerListener);

        // Nút Chặn người dùng ở cuối trang
        View btnBlock = findViewById(R.id.btnBlockUser);
        if (btnBlock != null) {
            btnBlock.setOnClickListener(v -> showBlockUserDialog());
        }

        // Nút Báo cáo người dùng ở cuối trang
        View btnReport = findViewById(R.id.btnReportUser);
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> showReportUserDialog());
        }
    }

    private void showBlockUserDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.lg_bottom_sheet_block_user, null);
        bottomSheetDialog.setContentView(view);

        // Nút Chặn
        View btnConfirm = view.findViewById(R.id.btnConfirmBlock);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();
                showBlockSuccessDialog();
            });
        }

        // Nút Hủy
        View btnCancel = view.findViewById(R.id.btnCancelBlock);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }

    private void showBlockSuccessDialog() {
        BlockSuccessDialog dialog = new BlockSuccessDialog(
                this,
                () -> {
                    Toast.makeText(this, "Đã chặn " + currentUserName + " thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileObjectActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );
        dialog.show();
    }

    private void showReportUserDialog() {
        Intent intent = new Intent(ProfileObjectActivity.this, ReportUserActivity.class);
        intent.putExtra(ReportUserActivity.EXTRA_NAME, currentUserName);
        startActivity(intent);
    }
}

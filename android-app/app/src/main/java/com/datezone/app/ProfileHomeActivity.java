package com.datezone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.profile_activity_home);

        View root = findViewById(R.id.myProfileRoot);
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

                // The root already reserves this space; children must not add it again.
                return insets.inset(bars.left, bars.top, bars.right, bars.bottom);
            });
            ViewCompat.requestApplyInsets(root);
        }

        setupButtons();
        setupBottomNavigation();
        loadSavedProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedProfileData();
    }

    private void loadSavedProfileData() {
        SharedPreferences prefs = getSharedPreferences(ProfileEditActivity.PREFS_NAME, Context.MODE_PRIVATE);

        String name = prefs.getString(ProfileEditActivity.KEY_NAME, "Trương Vĩnh Nguyên");
        String bio = prefs.getString(ProfileEditActivity.KEY_BIO, "Thích du lịch, cà phê và nghe nhạc indie.");
        String gender = prefs.getString(ProfileEditActivity.KEY_GENDER, "Nam");
        String height = prefs.getString(ProfileEditActivity.KEY_HEIGHT, "170 cm");
        String weight = prefs.getString(ProfileEditActivity.KEY_WEIGHT, "65 kg");
        String location = prefs.getString(ProfileEditActivity.KEY_LOCATION, "Hồ Chí Minh, Việt Nam");

        TextView tvName = findViewById(R.id.tvMyProfileName);
        if (tvName != null) tvName.setText(name);

        TextView tvAgeGender = findViewById(R.id.tvMyProfileAgeGender);
        if (tvAgeGender != null) tvAgeGender.setText("20 tuổi • Giới tính " + gender);

        TextView tvMeasurements = findViewById(R.id.tvMyProfileMeasurements);
        if (tvMeasurements != null) tvMeasurements.setText("Cao " + height + " • Nặng " + weight);

        TextView tvBio = findViewById(R.id.tvMyProfileBio);
        if (tvBio != null) tvBio.setText(bio);

        TextView tvLocation = findViewById(R.id.tvMyProfileLocation);
        if (tvLocation != null) tvLocation.setText("📍 " + location);
    }

    private void setupButtons() {
        View btnEdit = findViewById(R.id.btnMyProfileEdit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileHomeActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            });
        }

        View btnPreview = findViewById(R.id.btnMyProfilePreview);
        if (btnPreview != null) {
            btnPreview.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileHomeActivity.this, UserProfileActivity.class);
                startActivity(intent);
            });
        }

        View rowSettings = findViewById(R.id.rowMyProfileSettings);
        if (rowSettings != null) {
            rowSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileHomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.myProfileBottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navDiscoveryProfile);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navDiscoveryExplore) {
                    Intent intent = new Intent(ProfileHomeActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navDiscoveryLikes) {
                    Intent intent = new Intent(ProfileHomeActivity.this, MatchesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navDiscoveryChat) {
                    Intent intent = new Intent(ProfileHomeActivity.this, ChatListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (itemId == R.id.navDiscoveryProfile) {
                    return true;
                }
                return false;
            });
        }
    }
}

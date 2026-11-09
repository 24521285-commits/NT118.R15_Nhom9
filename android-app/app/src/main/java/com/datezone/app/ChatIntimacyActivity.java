package com.datezone.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ChatIntimacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.chat_activity_intimacy);

        View root = findViewById(R.id.chatIntimacyRoot);
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

        // Nút Back
        View btnBack = findViewById(R.id.btnBackIntimacy);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupActionButtons();
    }

    private void setupActionButtons() {
        // --- NGUYÊN ---
        View btnChatNguyen = findViewById(R.id.btnIntimacyChatNguyen);
        if (btnChatNguyen != null) {
            btnChatNguyen.setOnClickListener(v -> openChatDetail("Nguyên", R.drawable.discovery_photo_1, 20));
        }

        View btnCallNguyen = findViewById(R.id.btnIntimacyCallNguyen);
        if (btnCallNguyen != null) {
            btnCallNguyen.setOnClickListener(v -> openCall("Nguyên", R.drawable.discovery_photo_1, false));
        }

        View btnVideoNguyen = findViewById(R.id.btnIntimacyVideoNguyen);
        if (btnVideoNguyen != null) {
            btnVideoNguyen.setOnClickListener(v -> openCall("Nguyên", R.drawable.discovery_photo_1, true));
        }

        // --- PENELOPE ---
        View btnChatPenelope = findViewById(R.id.btnIntimacyChatPenelope);
        if (btnChatPenelope != null) {
            btnChatPenelope.setOnClickListener(v -> openChatDetail("Penelope", R.drawable.discovery_photo_4, 20));
        }

        View btnCallPenelope = findViewById(R.id.btnIntimacyCallPenelope);
        if (btnCallPenelope != null) {
            btnCallPenelope.setOnClickListener(v -> openCall("Penelope", R.drawable.discovery_photo_4, false));
        }

        View btnVideoPenelope = findViewById(R.id.btnIntimacyVideoPenelope);
        if (btnVideoPenelope != null) {
            btnVideoPenelope.setOnClickListener(v -> openCall("Penelope", R.drawable.discovery_photo_4, true));
        }

        // --- CÁC THẺ ĐANG TIẾN TỚI ---
        View cardGrace = findViewById(R.id.cardIntimacyGrace);
        if (cardGrace != null) {
            cardGrace.setOnClickListener(v -> openChatDetail("Grace", R.drawable.discovery_photo_1, 12));
        }

        View cardAbigail = findViewById(R.id.cardIntimacyAbigail);
        if (cardAbigail != null) {
            cardAbigail.setOnClickListener(v -> openChatDetail("Abigail", R.drawable.discovery_photo_2, 7));
        }

        View cardElizabeth = findViewById(R.id.cardIntimacyElizabeth);
        if (cardElizabeth != null) {
            cardElizabeth.setOnClickListener(v -> openChatDetail("Elizabeth", R.drawable.discovery_photo_3, 5));
        }
    }

    private void openChatDetail(String name, int photoResId, int initialMsgCount) {
        Intent intent = new Intent(ChatIntimacyActivity.this, ChatDetailActivity.class);
        intent.putExtra(ChatDetailActivity.EXTRA_NAME, name);
        intent.putExtra(ChatDetailActivity.EXTRA_PHOTO_RES_ID, photoResId);
        intent.putExtra(ChatDetailActivity.EXTRA_INITIAL_MSG_COUNT, initialMsgCount);
        startActivity(intent);
    }

    private void openCall(String name, int photoResId, boolean isVideoCall) {
        Intent intent = new Intent(ChatIntimacyActivity.this, ChatCallActivity.class);
        intent.putExtra(ChatCallActivity.EXTRA_NAME, name);
        intent.putExtra(ChatCallActivity.EXTRA_PHOTO_RES_ID, photoResId);
        intent.putExtra(ChatCallActivity.EXTRA_IS_VIDEO_CALL, isVideoCall);
        startActivity(intent);
    }
}

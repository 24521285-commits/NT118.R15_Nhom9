package com.datezone.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ReportSuccessDialog extends Dialog {

    public interface OnContinueClickListener {
        void onContinue();
    }

    private final String userName;
    private final OnContinueClickListener listener;

    public ReportSuccessDialog(@NonNull Context context, String userName, OnContinueClickListener listener) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.userName = userName;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lg_dialog_report_success);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        setCancelable(false);

        TextView tvMessage = findViewById(R.id.tvReportSuccessMessage);
        if (tvMessage != null && userName != null && !userName.isEmpty()) {
            tvMessage.setText("Cảm ơn bạn đã giúp Date Zone an toàn hơn.\n" + userName + " đã bị chặn và cuộc trò chuyện đã được ẩn. Đội ngũ của chúng tôi sẽ xem xét báo cáo trong vòng 24 giờ.");
        }

        Button btnContinue = findViewById(R.id.btnContinue);
        if (btnContinue != null) {
            btnContinue.setOnClickListener(v -> {
                dismiss();
                if (listener != null) {
                    listener.onContinue();
                }
            });
        }
    }
}

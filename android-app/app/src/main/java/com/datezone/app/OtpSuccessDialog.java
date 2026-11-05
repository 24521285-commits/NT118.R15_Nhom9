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

public class OtpSuccessDialog extends Dialog {

    public interface OnContinueClickListener {
        void onContinue();
    }

    private String customMessage;
    private OnContinueClickListener listener;

    public OtpSuccessDialog(@NonNull Context context) {
        super(context);
    }

    public OtpSuccessDialog(@NonNull Context context, String message, OnContinueClickListener listener) {
        super(context);
        this.customMessage = message;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lg_dialog_otp_success);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        setCancelable(false);

        if (customMessage != null) {
            TextView tvMessage = findViewById(R.id.tvSuccessMessage);
            if (tvMessage != null) {
                tvMessage.setText(customMessage);
            }
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

    public void setOnContinueClickListener(OnContinueClickListener listener) {
        this.listener = listener;
    }
}

package com.datezone.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;

public class BlockSuccessDialog extends Dialog {

    public interface OnContinueClickListener {
        void onContinue();
    }

    private final OnContinueClickListener listener;

    public BlockSuccessDialog(@NonNull Context context, OnContinueClickListener listener) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lg_dialog_block_success);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        setCancelable(false);

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

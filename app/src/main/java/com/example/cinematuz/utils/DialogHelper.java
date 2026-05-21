package com.example.cinematuz.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class DialogHelper {

    public interface OnInputConfirmListener {
        void onConfirm(String input);
    }

    public interface OnConfirmListener {
        void onConfirm();
    }

    private static MaterialAlertDialogBuilder getMaterialBuilder(Context context) {
        // Upewnij się, że ten styl istnieje w themes.xml
        return new MaterialAlertDialogBuilder(context, com.example.cinematuz.R.style.ThemeOverlay_CinematUZ_MaterialAlertDialog);
    }

    public static void showInputDialog(Context context, String title, String message, String hint, String positiveBtn, String negativeBtn, OnInputConfirmListener listener) {
        FrameLayout container = new FrameLayout(context);
        int padding = (int) (24 * context.getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding / 2, padding, 0);

        TextInputLayout textInputLayout = new TextInputLayout(context);
        textInputLayout.setHint(hint);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxCornerRadii(16f, 16f, 16f, 16f);

        TextInputEditText editText = new TextInputEditText(context);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setMaxLines(1);
        textInputLayout.addView(editText);
        container.addView(textInputLayout);

        getMaterialBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(container)
                .setPositiveButton(positiveBtn, (dialog, which) -> {
                    if (listener != null && editText.getText() != null) {
                        listener.onConfirm(editText.getText().toString().trim());
                    }
                })
                .setNegativeButton(negativeBtn, null)
                .show();
    }

    public static void showConfirmDialog(Context context, String title, String message, String pos, String neg, OnConfirmListener listener) {
        getMaterialBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pos, (dialog, which) -> { if(listener != null) listener.onConfirm(); })
                .setNegativeButton(neg, null)
                .show();
    }

    public static void showItemsDialog(Context context, String title, String[] items, DialogInterface.OnClickListener listener) {
        getMaterialBuilder(context)
                .setTitle(title)
                .setItems(items, listener)
                .show();
    }
}
package com.example.cinematuz.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.FrameLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Klasa pomocnicza ułatwiająca tworzenie i wyświetlanie spójnych wizualnie okien dialogowych (Material Design).
 * Zawiera metody do wyświetlania dialogów z polem tekstowym, potwierdzeń oraz list wyboru.
 */
public class DialogHelper {

    /**
     * Interfejs callbacku dla dialogów z wprowadzaniem tekstu.
     */
    public interface OnInputConfirmListener {
        /**
         * Wywoływane, gdy użytkownik zatwierdzi wprowadzony tekst.
         * @param input Wprowadzony ciąg znaków.
         */
        void onConfirm(String input);
    }

    /**
     * Interfejs callbacku dla dialogów potwierdzających operację.
     */
    public interface OnConfirmListener {
        /**
         * Wywoływane po kliknięciu przycisku potwierdzenia.
         */
        void onConfirm();
    }

    /**
     * Tworzy bazowy kreator dialogów Material z nałożonym stylem aplikacji.
     */
    private static MaterialAlertDialogBuilder getMaterialBuilder(Context context) {
        return new MaterialAlertDialogBuilder(context, com.example.cinematuz.R.style.ThemeOverlay_CinematUZ_MaterialAlertDialog);
    }

    /**
     * Wyświetla okno dialogowe z polem tekstowym.
     * 
     * @param context Kontekst.
     * @param title Tytuł okna.
     * @param message Treść komunikatu.
     * @param hint Podpowiedź w polu tekstowym.
     * @param positiveBtn Tekst przycisku potwierdzenia.
     * @param negativeBtn Tekst przycisku anulowania.
     * @param listener Listener wyniku.
     */
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

    /**
     * Wyświetla okno dialogowe z pytaniem potwierdzającym (Tak/Nie).
     * 
     * @param context Kontekst.
     * @param title Tytuł okna.
     * @param message Treść pytania.
     * @param pos Tekst przycisku twierdzącego.
     * @param neg Tekst przycisku przeczącego.
     * @param listener Listener wywołany przy potwierdzeniu.
     */
    public static void showConfirmDialog(Context context, String title, String message, String pos, String neg, OnConfirmListener listener) {
        getMaterialBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pos, (dialog, which) -> { if(listener != null) listener.onConfirm(); })
                .setNegativeButton(neg, null)
                .show();
    }

    /**
     * Wyświetla okno dialogowe z listą elementów do wyboru.
     * 
     * @param context Kontekst.
     * @param title Tytuł okna.
     * @param items Tablica tekstów do wyświetlenia na liście.
     * @param listener Listener wyboru elementu.
     */
    public static void showItemsDialog(Context context, String title, String[] items, DialogInterface.OnClickListener listener) {
        getMaterialBuilder(context)
                .setTitle(title)
                .setItems(items, listener)
                .show();
    }
}
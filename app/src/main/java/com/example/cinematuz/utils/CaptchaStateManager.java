package com.example.cinematuz.utils;

import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.material.card.MaterialCardView;

/**
 * Klasa zarządzająca stanem komponentów hCaptcha w interfejsie użytkownika.
 * Odpowiada za blokowanie przycisków, aktualizację checkboxa oraz przechowywanie tokenu weryfikacji.
 */
public class CaptchaStateManager {

    private final MaterialCardView captchaContainer;
    private final CheckBox captchaCheckBox;
    private final Button submitButton;

    private String captchaToken;

    /**
     * Konstruktor menedżera stanu captcha.
     * 
     * @param captchaContainer Kontener (karta) zawierający widżet captcha.
     * @param captchaCheckBox Checkbox informujący o statusie weryfikacji.
     * @param submitButton Przycisk zatwierdzający formularz (np. zaloguj/zarejestruj).
     */
    public CaptchaStateManager(MaterialCardView captchaContainer, CheckBox captchaCheckBox, Button submitButton) {
        this.captchaContainer = captchaContainer;
        this.captchaCheckBox = captchaCheckBox;
        this.submitButton = submitButton;
    }

    /**
     * Wywoływane po pomyślnej weryfikacji przez użytkownika.
     * 
     * @param token Token wygenerowany przez hCaptcha SDK.
     */
    public void onCaptchaVerified(String token) {
        captchaToken = token;
        if (captchaCheckBox != null) {
            captchaCheckBox.setChecked(true);
        }
        if (captchaContainer != null) {
            captchaContainer.setClickable(false);
        }
    }

    /**
     * Resetuje stan captcha do początkowego (np. po błędzie serwera).
     */
    public void onCaptchaReset() {
        captchaToken = null;
        if (captchaCheckBox != null) {
            captchaCheckBox.setChecked(false);
        }
        if (captchaContainer != null) {
            captchaContainer.setClickable(true);
        }
        setSubmitEnabled(true);
    }

    /**
     * Blokuje przycisk wysyłania po rozpoczęciu procesu logowania/rejestracji.
     */
    public void onSubmitStarted() {
        setSubmitEnabled(false);
    }

    /**
     * Odblokowuje przycisk wysyłania po zakończeniu procesu.
     */
    public void onSubmitFinished() {
        setSubmitEnabled(true);
    }

    /**
     * Sprawdza, czy użytkownik przeszedł weryfikację captcha.
     * 
     * @return true, jeśli token jest obecny.
     */
    public boolean hasVerifiedCaptcha() {
        return captchaToken != null && !captchaToken.trim().isEmpty();
    }

    /**
     * Zwraca aktualny token weryfikacji.
     * 
     * @return Token hCaptcha.
     */
    public String getCaptchaToken() {
        return captchaToken;
    }

    private void setSubmitEnabled(boolean enabled) {
        if (submitButton != null) {
            submitButton.setEnabled(enabled);
        }
    }
}

package com.example.cinematuz.utils;

import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.material.card.MaterialCardView;

/**
 * Wspolna logika stanu captcha dla ekranow logowania i rejestracji.
 */
public class CaptchaStateManager {

    private final MaterialCardView captchaContainer;
    private final CheckBox captchaCheckBox;
    private final Button submitButton;

    private String captchaToken;

    public CaptchaStateManager(MaterialCardView captchaContainer, CheckBox captchaCheckBox, Button submitButton) {
        this.captchaContainer = captchaContainer;
        this.captchaCheckBox = captchaCheckBox;
        this.submitButton = submitButton;
    }

    public void onCaptchaVerified(String token) {
        captchaToken = token;
        if (captchaCheckBox != null) {
            captchaCheckBox.setChecked(true);
        }
        if (captchaContainer != null) {
            captchaContainer.setClickable(false);
        }
    }

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

    public void onSubmitStarted() {
        setSubmitEnabled(false);
    }

    public void onSubmitFinished() {
        setSubmitEnabled(true);
    }

    public boolean hasVerifiedCaptcha() {
        return captchaToken != null && !captchaToken.trim().isEmpty();
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    private void setSubmitEnabled(boolean enabled) {
        if (submitButton != null) {
            submitButton.setEnabled(enabled);
        }
    }
}


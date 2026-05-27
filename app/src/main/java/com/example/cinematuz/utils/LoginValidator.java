package com.example.cinematuz.utils;

public class LoginValidator {

    // Sprawdza, czy email ma sens (nie jest pusty, ma '@' i kropkę)
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    // Sprawdza, czy hasło ma co najmniej 6 znaków (standard Firebase)
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return password.length() >= 6;
    }
}
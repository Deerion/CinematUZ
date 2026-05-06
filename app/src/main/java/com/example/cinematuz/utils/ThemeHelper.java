package com.example.cinematuz.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.cinematuz.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_IS_DARK_MODE = "is_dark_mode";
    private static final String TAG = "ThemeHelper";

    public static void applyTheme(Context context) {
        if (isDarkMode(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void setDarkMode(Context context, boolean isDark) {
        // Użycie getApplicationContext() zapobiega wyciekom pamięci
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_IS_DARK_MODE, isDark).apply();
        applyTheme(context);
    }

    public static boolean isDarkMode(Context context) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Domyślnie true, ponieważ LoginActivity wymusza tryb ciemny
        return prefs.getBoolean(KEY_IS_DARK_MODE, true);
    }

    public static void applyMapStyle(Context context, GoogleMap googleMap) {
        if (googleMap == null) return;

        try {
            int styleResourceId = isDarkMode(context) ? R.raw.map_style_dark : R.raw.map_style_light;

            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, styleResourceId)
            );

            if (!success) {
                Log.e(TAG, "Parsowanie stylu mapy się nie powiodło.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Nie znaleziono pliku stylu mapy. Upewnij się, że pliki map_style_light.json i map_style_dark.json są w res/raw.", e);
        }
    }
}
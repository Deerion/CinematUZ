package com.example.cinematuz.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * Klasa pomocnicza do zarządzania lokalizacją (językiem) aplikacji.
 * Odpowiada za zapisywanie wybranego języka w ustawieniach oraz aktualizację
 * konfiguracji zasobów (Resources) w celu dynamicznej zmiany języka interfejsu.
 */
public class LocaleHelper {
    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    /**
     * Wywoływane przy starcie aplikacji (np. w Activity) w celu nałożenia zapisanego języka.
     * 
     * @param context Kontekst wejściowy.
     * @return Kontekst z nową konfiguracją językową.
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    /**
     * Wywoływane przy starcie aplikacji z określonym językiem domyślnym.
     * 
     * @param context Kontekst wejściowy.
     * @param defaultLanguage Domyślny kod języka.
     * @return Kontekst z nową konfiguracją językową.
     */
    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    /**
     * Zwraca aktualnie ustawiony kod języka w aplikacji.
     * 
     * @param context Kontekst aplikacji.
     * @return Kod języka (np. "pl", "en").
     */
    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    /**
     * Zmienia język aplikacji, zapisuje go w preferencjach i aktualizuje kontekst.
     * 
     * @param context Kontekst aplikacji.
     * @param language Nowy kod języka.
     * @return Kontekst z zaktualizowaną lokalizacją.
     */
    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }
        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}

package com.example.cinematuz.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cinematuz.R;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        View languageTile = view.findViewById(R.id.language_settings_tile);
        TextView textPl = view.findViewById(R.id.textPl);
        TextView textEn = view.findViewById(R.id.textEn);

        // Pobranie aktualnie zapisanego języka
        String currentLang = LocaleHelper.getLanguage(requireContext());

        if ("en".equals(currentLang)) {
            // Styl dla włączonego EN
            textEn.setBackgroundResource(R.drawable.bg_switch_active);
            textEn.setTextColor(Color.parseColor("#FFFFFF"));
            // Styl dla wyłączonego PL
            textPl.setBackgroundResource(android.R.color.transparent);
            textPl.setTextColor(Color.parseColor("#9E9E9E"));
        } else {
            // Styl dla włączonego PL (domyślny)
            textPl.setBackgroundResource(R.drawable.bg_switch_active);
            textPl.setTextColor(Color.parseColor("#FFFFFF"));
            // Styl dla wyłączonego EN
            textEn.setBackgroundResource(android.R.color.transparent);
            textEn.setTextColor(Color.parseColor("#9E9E9E"));
        }

        // Obsługa kliknięcia w przycisk "PL"
        textPl.setOnClickListener(v -> {
            if (!"pl".equals(LocaleHelper.getLanguage(requireContext()))) {
                LocaleHelper.setLocale(requireContext(), "pl");
                requireActivity().recreate(); // Przeładuj aktywność po zmianie
            }
        });

        // Obsługa kliknięcia w przycisk "EN"
        textEn.setOnClickListener(v -> {
            if (!"en".equals(LocaleHelper.getLanguage(requireContext()))) {
                LocaleHelper.setLocale(requireContext(), "en");
                requireActivity().recreate(); // Przeładuj aktywność po zmianie
            }
        });

        // Opcjonalnie: Obsługa kliknięcia w dowolne miejsce kafelka działa jak Toggle (przełącznik)
        languageTile.setOnClickListener(v -> {
            String lang = LocaleHelper.getLanguage(requireContext());
            if ("pl".equals(lang)) {
                LocaleHelper.setLocale(requireContext(), "en");
            } else {
                LocaleHelper.setLocale(requireContext(), "pl");
            }
            requireActivity().recreate();
        });

        // --- OBSŁUGA MOTYWU ---
        View themeTile = view.findViewById(R.id.theme_settings_tile);
        TextView textThemeLight = view.findViewById(R.id.textThemeLight);
        TextView textThemeDark = view.findViewById(R.id.textThemeDark);

        boolean isDark = ThemeHelper.isDarkMode(requireContext());

        if (isDark) {
            setActiveStyle(textThemeDark);
            setInactiveStyle(textThemeLight);
        } else {
            setActiveStyle(textThemeLight);
            setInactiveStyle(textThemeDark);
        }

        textThemeLight.setOnClickListener(v -> {
            if (ThemeHelper.isDarkMode(requireContext())) {
                ThemeHelper.setDarkMode(requireContext(), false);
                requireActivity().recreate();
            }
        });

        textThemeDark.setOnClickListener(v -> {
            if (!ThemeHelper.isDarkMode(requireContext())) {
                ThemeHelper.setDarkMode(requireContext(), true);
                requireActivity().recreate();
            }
        });

        themeTile.setOnClickListener(v -> {
            boolean current = ThemeHelper.isDarkMode(requireContext());
            ThemeHelper.setDarkMode(requireContext(), !current);
            requireActivity().recreate();
        });

        return view;
    }

    private void setActiveStyle(TextView textView) {
        textView.setBackgroundResource(R.drawable.bg_switch_active);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void setInactiveStyle(TextView textView) {
        textView.setBackgroundResource(android.R.color.transparent);
        textView.setTextColor(Color.parseColor("#9E9E9E"));
    }
}
package com.example.cinematuz.ui.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cinematuz.R;
import com.example.cinematuz.ui.activities.LoginActivity;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public ProfileFragment() {
        // Wymagany pusty konstruktor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Konfiguracja Google Sign-In do pełnego wylogowania
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // --- OBSŁUGA JĘZYKA ---
        View languageTile = view.findViewById(R.id.language_settings_tile);
        TextView textPl = view.findViewById(R.id.textPl);
        TextView textEn = view.findViewById(R.id.textEn);

        String currentLang = LocaleHelper.getLanguage(requireContext());
        if ("en".equals(currentLang)) {
            setActiveStyle(textEn);
            setInactiveStyle(textPl);
        } else {
            setActiveStyle(textPl);
            setInactiveStyle(textEn);
        }

        textPl.setOnClickListener(v -> changeLanguage("pl"));
        textEn.setOnClickListener(v -> changeLanguage("en"));
        languageTile.setOnClickListener(v -> {
            String nextLang = "pl".equals(LocaleHelper.getLanguage(requireContext())) ? "en" : "pl";
            changeLanguage(nextLang);
        });

        // --- OBSŁUGA MOTYWU ---
        View themeTile = view.findViewById(R.id.theme_settings_tile);
        TextView textThemeLight = view.findViewById(R.id.textThemeLight);
        TextView textThemeDark = view.findViewById(R.id.textThemeDark);

        if (ThemeHelper.isDarkMode(requireContext())) {
            setActiveStyle(textThemeDark);
            setInactiveStyle(textThemeLight);
        } else {
            setActiveStyle(textThemeLight);
            setInactiveStyle(textThemeDark);
        }

        textThemeLight.setOnClickListener(v -> toggleTheme(false));
        textThemeDark.setOnClickListener(v -> toggleTheme(true));
        themeTile.setOnClickListener(v -> toggleTheme(!ThemeHelper.isDarkMode(requireContext())));

        // --- OBSŁUGA WYLOGOWANIA ---
        View logoutTile = view.findViewById(R.id.logout_settings_tile);

        if (mAuth.getCurrentUser() != null) {
            logoutTile.setVisibility(View.VISIBLE);
            logoutTile.setOnClickListener(v -> performLogout());
        } else {
            logoutTile.setVisibility(View.GONE);
        }

        return view;
    }

    private void changeLanguage(String langCode) {
        if (!langCode.equals(LocaleHelper.getLanguage(requireContext()))) {
            LocaleHelper.setLocale(requireContext(), langCode);
            requireActivity().recreate();
        }
    }

    private void toggleTheme(boolean dark) {
        if (dark != ThemeHelper.isDarkMode(requireContext())) {
            ThemeHelper.setDarkMode(requireContext(), dark);
            requireActivity().recreate();
        }
    }

    private void performLogout() {
        // 1. Wylogowanie z Firebase
        mAuth.signOut();

        // 2. Wylogowanie z Google (czyści zapamiętane konto)
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(requireContext(), "Wylogowano pomyślnie", Toast.LENGTH_SHORT).show();

            // 3. Przejście do LoginActivity z wyczyszczeniem stosu aktywności
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Zamykamy MainActivity
            requireActivity().finish();
        });
    }

    private void setActiveStyle(TextView textView) {
        if (textView != null) {
            textView.setBackgroundResource(R.drawable.bg_switch_active);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void setInactiveStyle(TextView textView) {
        if (textView != null) {
            textView.setBackgroundResource(android.R.color.transparent);
            textView.setTextColor(Color.parseColor("#9E9E9E"));
        }
    }
}
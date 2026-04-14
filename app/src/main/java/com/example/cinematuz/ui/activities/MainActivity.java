package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.cinematuz.R;
import com.example.cinematuz.databinding.ActivityMainBinding;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    // ViewBinding dla głównego kontenera aplikacji.
    private ActivityMainBinding binding;

    // ---------------- Lifecycle ----------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setupWindow();
        setupBinding();
        setupInsets();
        setupNavigation();
    }

    // ---------------- Kontekst lokalizacji ----------------

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    // ---------------- Konfiguracja UI ----------------

    private void setupWindow() {
        // Edge-to-edge: sami kontrolujemy odsunięcia przez WindowInsets.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    private void setupBinding() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            binding.navView.setPadding(0, 0, 0, systemBars.bottom);
            return windowInsets;
        });
    }

    // ---------------- Nawigacja ----------------

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);
        binding.navView.setVisibility(View.VISIBLE);
    }
}
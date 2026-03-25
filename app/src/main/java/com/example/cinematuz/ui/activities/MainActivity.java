package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.cinematuz.R;
import com.example.cinematuz.databinding.ActivityMainBinding;
import com.example.cinematuz.ui.fragments.HomeFragment;
import com.example.cinematuz.ui.fragments.FriendsFragment;
import com.example.cinematuz.ui.fragments.MapFragment;
import com.example.cinematuz.ui.fragments.ProfileFragment;
import com.example.cinematuz.utils.LocaleHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Mówimy systemowi, że sami obsłużymy odstępy (żeby paski nie nachodziły na UI)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Naprawiamy górny pasek (Toolbar) i dolny (BottomNav), żeby nie chowały się pod system
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Dodajemy padding na górze (dla Toolbaru) i na dole (dla NavView)
            binding.appBarLayout.setPadding(0, systemBars.top, 0, 0);
            binding.navView.setPadding(0, 0, 0, systemBars.bottom);

            return windowInsets;
        });

        // Ustawienie górnego paska (Toolbar)
        setSupportActionBar(binding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Wczytanie domyślnego fragmentu (Start) po uruchomieniu aplikacji
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Logika obsługująca kliknięcia w dolnym pasku
        binding.navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_start) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_friends) {
                selectedFragment = new FriendsFragment();
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Ta metoda upewnia się, że Activity użyje języka zapisanego w LocaleHelper
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
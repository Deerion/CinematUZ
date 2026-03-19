package com.example.cinematuz.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cinematuz.R;
import com.example.cinematuz.databinding.ActivityMainBinding;
import com.example.cinematuz.ui.fragments.HomeFragment;
import com.example.cinematuz.ui.fragments.FriendsFragment;
import com.example.cinematuz.ui.fragments.MapFragment;
import com.example.cinematuz.ui.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ustawienie górnego paska (Toolbar)
        setSupportActionBar(binding.toolbar);

        // 1. Wczytanie domyślnego fragmentu (Start) po uruchomieniu aplikacji
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // 2. Logika obsługująca kliknięcia w dolnym pasku
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Przypisanie odpowiedniego fragmentu do klikniętej ikony
            if (itemId == R.id.nav_start) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_friends) {
                selectedFragment = new FriendsFragment();
            } else if (itemId == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            // Jeśli fragment został pomyślnie wybrany, podmień go na ekranie
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true; // Zwracamy true, aby ikona na pasku zaświeciła się jako aktywna
            }

            return false;
        });
    }

    // Obsługa górnego menu (np. Ustawienia)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Tutaj w przyszłości można dodać przejście do ustawień
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package com.example.cinematuz.ui.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.User;
import com.example.cinematuz.databinding.ActivityMainBinding;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.StatisticsWidgetProvider;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Główna aktywność aplikacji zarządzająca nawigacją dolną i synchronizacją statystyk dla widgetu.
 * Odpowiada za hostowanie fragmentów i konfigurację głównego interfejsu użytkownika.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Inicjalizuje aktywność, konfiguruje nawigację, insets oraz nasłuchiwanie statystyk.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setupWindow();
        setupBinding();
        setupInsets();
        setupNavigation();

        // Uruchamiamy nasłuchiwanie statystyk dla Widgetu w tle
        setupStatisticsListener();
    }

    /**
     * Dołącza kontekst z obsługą wybranego języka.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    /**
     * Konfiguruje okno aplikacji do pracy w trybie "edge-to-edge".
     */
    private void setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    /**
     * Inicjalizuje ViewBinding dla aktywności.
     */
    private void setupBinding() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    /**
     * Konfiguruje paddingi widoku głównego w oparciu o paski systemowe.
     */
    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // ZMIANA TUTAJ: Ostatni argument to teraz 0. Usuwamy podwójny dolny margines!
            v.setPadding(0, systemBars.top, 0, 0);

            return windowInsets;
        });
    }

    /**
     * Konfiguruje Navigation Component z BottomNavigationView.
     * Obsługuje widoczność paska nawigacji na różnych ekranach oraz specyficzne zachowanie dla biblioteki.
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.navView.setOnItemSelectedListener(item -> {
            int destinationId = item.getItemId();

            if (destinationId == R.id.nav_library) {
                // Konfiguracja dla Biblioteki
                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setRestoreState(false) // <-- Wyłącza pamięć historii dla tej zakładki
                        .setPopUpTo(
                                navController.getGraph().getStartDestinationId(),
                                false,
                                true
                        )
                        .build();

                navController.navigate(destinationId, null, options);
                return true;
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        binding.navView.setVisibility(View.VISIBLE);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Sprawdzamy czy to shaker lub zwycięzca
            if (destination.getId() == R.id.shakeFragment || destination.getId() == R.id.winnerFragment) {
                binding.navView.setVisibility(View.GONE);
            } else {
                // Na każdym innym ekranie pasek będzie widoczny
                binding.navView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Konfiguruje nasłuchiwanie zmian w statystykach użytkownika w Firestore.
     * Po zmianie danych aktualizuje SharedPreferences i odświeża widget aplikacji.
     */
    private void setupStatisticsListener() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String myUid = auth.getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection("profiles").document(myUid)
                    .addSnapshotListener((doc, e) -> {
                        if (e != null || doc == null || !doc.exists()) return;

                        // Konwertujemy dokument na obiekt User, aby wyciągnąć nowe statystyki
                        User userProfile = doc.toObject(User.class);
                        if (userProfile != null && userProfile.getStats() != null) {

                            int movies = userProfile.getStats().getMoviesWatched();
                            int series = userProfile.getStats().getTvShowsWatched();

                            // Zapisujemy najświeższe dane do SharedPreferences dla Widgetu
                            SharedPreferences.Editor editor = getSharedPreferences("CinematUZ_Stats", MODE_PRIVATE).edit();
                            editor.putInt("movies_count", movies);
                            editor.putInt("tv_shows_count", series);
                            editor.apply();

                            // Wysyłamy sygnał do odświeżenia Widgetu na pulpicie
                            Intent intent = new Intent(this, StatisticsWidgetProvider.class);
                            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                            int[] ids = AppWidgetManager.getInstance(this)
                                    .getAppWidgetIds(new ComponentName(this, StatisticsWidgetProvider.class));
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                            sendBroadcast(intent);
                        }
                    });
        }
    }
}
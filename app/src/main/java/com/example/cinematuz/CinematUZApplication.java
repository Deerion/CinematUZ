package com.example.cinematuz;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.cinematuz.utils.AppKillDetectionService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Główna klasa aplikacji CinematUZ, inicjalizująca globalne usługi i monitorująca cykl życia aplikacji.
 */
public class CinematUZApplication extends Application {

    /**
     * Wywoływane przy tworzeniu aplikacji. Inicjalizuje serwis wykrywania zabicia aplikacji
     * oraz obserwatora cyklu życia procesu do zarządzania statusem online użytkownika.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Zabezpieczamy uruchomienie serwisu blokiem try-catch.
        // W trakcie testów Espresso system może zablokować start serwisu w tle, co wyrzuca błąd.
        try {
            startService(new Intent(this, AppKillDetectionService.class));
        } catch (Exception e) {
            // Ignorujemy błąd. Serwis nie wystartuje w środowisku testowym,
            // ale zapobiega to wyrzuceniu crasha i pozwala wykonać testy UI.
            e.printStackTrace();
        }

        // 2. Nasłuchujemy, kiedy użytkownik wchodzi do aplikacji
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStart(@NonNull LifecycleOwner owner) {
                // Aplikacja otwarta lub wybudzona z tła -> Aktywny teraz
                setOnlineStatus(true);
            }

            // UWAGA: Metoda onStop() została całkowicie usunięta!
            // Dzięki temu wejście do menu telefonu nie powoduje zmiany statusu na offline.
        });
    }

    /**
     * Aktualizuje status online użytkownika w bazie danych Firestore.
     * 
     * @param isOnline Wartość logiczna określająca, czy użytkownik jest online.
     */
    private void setOnlineStatus(boolean isOnline) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String myUid = auth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("profiles").document(myUid)
                    .update("isOnline", isOnline);
        }
    }
}
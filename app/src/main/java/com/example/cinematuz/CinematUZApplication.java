// Lokalizacja: java/com/example/cinematuz/CinematUZApplication.java
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

public class CinematUZApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Uruchamiamy nasz cichy serwis do wykrywania "ubicia" aplikacji
        startService(new Intent(this, AppKillDetectionService.class));

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

    private void setOnlineStatus(boolean isOnline) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String myUid = auth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("profiles").document(myUid)
                    .update("isOnline", isOnline);
        }
    }
}
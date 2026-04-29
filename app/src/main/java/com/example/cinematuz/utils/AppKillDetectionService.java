package com.example.cinematuz.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppKillDetectionService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String myUid = auth.getCurrentUser().getUid();

            // Zmieniamy status na offline
            FirebaseFirestore.getInstance().collection("profiles").document(myUid)
                    .update("isOnline", false);

            // --- NOWE: Dajemy Firebase pół sekundy na wysłanie danych do bazy ---
            // Zanim Android brutalnie zabije proces, wymuszamy krótką przerwę.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stopSelf();
    }
}
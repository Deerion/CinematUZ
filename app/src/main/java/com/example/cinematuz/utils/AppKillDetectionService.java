// Lokalizacja: java/com/example/cinematuz/utils/AppKillDetectionService.java
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
        return null; // Bindowanie nie jest nam potrzebne
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Serwis ma działać cicho w tle i nie odradzać się sam
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // TEN KOD WYKONUJE SIĘ W MOMENCIE "UBICIA" APLIKACJI
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String myUid = auth.getCurrentUser().getUid();
            // Zmieniamy status w Firebase na offline
            FirebaseFirestore.getInstance().collection("profiles").document(myUid)
                    .update("isOnline", false);
        }

        // Zabijamy serwis, aby nie obciążał telefonu
        stopSelf();
    }
}
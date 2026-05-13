package com.example.cinematuz.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;

public class NearbyHelper {
    private static final String SERVICE_ID = "com.example.cinematuz.NEARBY_FRIENDS";
    private final Context context;
    private final NearbyDiscoveryListener listener;
    private String myUid;

    public interface NearbyDiscoveryListener {
        void onUserFound(String uid); // Zwracamy UID znalezionego użytkownika
    }

    public NearbyHelper(Context context, String myUid, NearbyDiscoveryListener listener) {
        this.context = context;
        this.myUid = myUid;
        this.listener = listener;
    }

    public void startSearching() {
        startAdvertising();
        startDiscovery();
    }

    public void stopSearching() {
        Nearby.getConnectionsClient(context).stopAdvertising();
        Nearby.getConnectionsClient(context).stopDiscovery();
    }

    // Ogłaszamy w eterze nasz UID
    private void startAdvertising() {
        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        // Jako "Endpoint Name" nadajemy nasz UID. Dzięki temu inni od razu go znają bez łączenia się.
        Nearby.getConnectionsClient(context)
                .startAdvertising(myUid, SERVICE_ID, connectionLifecycleCallback, options)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> {});
    }

    // Szukamy w eterze innych UID
    private void startDiscovery() {
        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> {});
    }

    // Ktoś został znaleziony w okolicy!
    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            String discoveredUid = info.getEndpointName(); // To jest UID z Firebase!
            if (listener != null) {
                listener.onUserFound(discoveredUid);
            }
        }
        @Override
        public void onEndpointLost(@NonNull String endpointId) {}
    };

    // Puste callbacki - nie musimy się z nimi parować, żeby dostać UID
    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {}
        @Override public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {}
        @Override public void onDisconnected(@NonNull String s) {}
    };
}
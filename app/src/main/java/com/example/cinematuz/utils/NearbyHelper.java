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

/**
 * Klasa pomocnicza dla Google Nearby Connections API.
 * Umożliwia wykrywanie innych użytkowników w pobliżu bez konieczności nawiązywania pełnego połączenia,
 * wykorzystując identyfikator UID jako nazwę punktu końcowego (endpoint name).
 */
public class NearbyHelper {
    private static final String SERVICE_ID = "com.example.cinematuz.NEARBY_FRIENDS";
    private final Context context;
    private final NearbyDiscoveryListener listener;
    private String myUid;

    /**
     * Interfejs powiadamiający o znalezieniu identyfikatora użytkownika w pobliżu.
     */
    public interface NearbyDiscoveryListener {
        /**
         * Wywoływane, gdy zostanie wykryty użytkownik z określonym UID.
         * @param uid Identyfikator UID znalezionego użytkownika.
         */
        void onUserFound(String uid);
    }

    /**
     * Konstruktor NearbyHelper.
     * 
     * @param context Kontekst aplikacji.
     * @param myUid Identyfikator UID bieżącego użytkownika (do ogłaszania).
     * @param listener Listener wyników wykrywania.
     */
    public NearbyHelper(Context context, String myUid, NearbyDiscoveryListener listener) {
        this.context = context;
        this.myUid = myUid;
        this.listener = listener;
    }

    /**
     * Uruchamia jednocześnie rozgłaszanie (advertising) własnego identyfikatora
     * oraz wykrywanie (discovery) identyfikatorów innych osób.
     */
    public void startSearching() {
        startAdvertising();
        startDiscovery();
    }

    /**
     * Zatrzymuje procesy rozgłaszania i wykrywania Nearby Connections.
     */
    public void stopSearching() {
        Nearby.getConnectionsClient(context).stopAdvertising();
        Nearby.getConnectionsClient(context).stopDiscovery();
    }

    /**
     * Rozpoczyna ogłaszanie własnego UID w sieci lokalnej (Bluetooth/WiFi).
     */
    private void startAdvertising() {
        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
                .startAdvertising(myUid, SERVICE_ID, connectionLifecycleCallback, options)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> {});
    }

    /**
     * Rozpoczyna skanowanie w poszukiwaniu innych urządzeń korzystających z tego samego SERVICE_ID.
     */
    private void startDiscovery() {
        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        Nearby.getConnectionsClient(context)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> {});
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            String discoveredUid = info.getEndpointName();
            if (listener != null) {
                listener.onUserFound(discoveredUid);
            }
        }
        @Override
        public void onEndpointLost(@NonNull String endpointId) {}
    };

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {}
        @Override public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {}
        @Override public void onDisconnected(@NonNull String s) {}
    };
}
package com.example.cinematuz.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Klasa pomocnicza do obsługi podstawowych operacji Bluetooth, takich jak wykrywanie urządzeń.
 * Zarządza procesem skanowania i powiadamianiem o znalezionych urządzeniach.
 */
public class BluetoothHelper {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final BluetoothDiscoveryListener listener;

    /**
     * Interfejs powiadamiający o postępach wykrywania urządzeń.
     */
    public interface BluetoothDiscoveryListener {
        /**
         * Wywoływane, gdy zostanie znalezione nowe urządzenie Bluetooth.
         * @param device Obiekt znalezionego urządzenia.
         */
        void onDeviceFound(BluetoothDevice device);
        /**
         * Wywoływane po zakończeniu procesu skanowania.
         */
        void onDiscoveryFinished();
    }

    /**
     * Konstruktor klasy BluetoothHelper.
     * 
     * @param context Kontekst aplikacji.
     * @param listener Listener zdarzeń wykrywania.
     */
    public BluetoothHelper(Context context, BluetoothDiscoveryListener listener) {
        this.context = context;
        this.listener = listener;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (listener != null && device != null) listener.onDeviceFound(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (listener != null) listener.onDiscoveryFinished();
            }
        }
    };

    /**
     * Uruchamia proces wykrywania urządzeń Bluetooth w pobliżu.
     * Rejestruje odpowiedni BroadcastReceiver i wywołuje systemowe skanowanie.
     */
    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(receiver, filter);
            bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * Zatrzymuje proces wykrywania urządzeń i wyrejestrowuje BroadcastReceiver.
     */
    @SuppressLint("MissingPermission")
    public void stopDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception e) {
            // Ignoruj jeśli nie był zarejestrowany
        }
    }

    /**
     * Sprawdza, czy moduł Bluetooth jest włączony na urządzeniu.
     * 
     * @return true, jeśli Bluetooth jest włączony.
     */
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}
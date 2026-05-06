// Plik: main/java/com/example/cinematuz/utils/BluetoothHelper.java
package com.example.cinematuz.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.List;

public class BluetoothHelper {
    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;
    private final BluetoothDiscoveryListener listener;

    public interface BluetoothDiscoveryListener {
        void onDeviceFound(BluetoothDevice device);
        void onDiscoveryFinished();
    }

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

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}
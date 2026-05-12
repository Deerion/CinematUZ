package com.example.cinematuz.ui.fragments.friends.znajomi;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinematuz.R;
import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private final List<BluetoothDevice> devices;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        String name = device.getName();
        holder.tvName.setText(name != null ? name : "Nieznane urządzenie");
        holder.tvRange.setText("Zasięg: W zasięgu");
    }

    @Override
    public int getItemCount() { return devices.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRange;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvDeviceName);
            tvRange = v.findViewById(R.id.tvDeviceRange);
        }
    }
}
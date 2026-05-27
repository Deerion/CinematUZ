package com.example.cinematuz.ui.fragments.friends.znajomi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.SearchResultUser;
import com.google.android.material.button.MaterialButton;
import java.util.List;

/**
 * Adapter dla listy urządzeń/użytkowników wykrytych przez Bluetooth (Nearby).
 * Umożliwia wysyłanie zaproszeń do grup do osób znajdujących się w pobliżu.
 */
public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private final List<SearchResultUser> users;
    private final OnInviteClickListener listener;

    /**
     * Interfejs obsługujący kliknięcie przycisku zaproszenia.
     */
    public interface OnInviteClickListener {
        /**
         * Wywoływane przy próbie wysłania zaproszenia.
         * 
         * @param user Użytkownik, który ma zostać zaproszony.
         * @return true, jeśli zaproszenie zostało pomyślnie wysłane, false w przeciwnym razie.
         */
        boolean onInvite(SearchResultUser user);
    }

    /**
     * Konstruktor adaptera.
     * 
     * @param users Lista użytkowników wykrytych w pobliżu.
     * @param listener Listener akcji zaproszenia.
     */
    public BluetoothDeviceAdapter(List<SearchResultUser> users, OnInviteClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bluetooth_device, parent, false));
    }

    /**
     * Wiąże dane użytkownika z widokiem elementu listy.
     * Obsługuje ładowanie awatara oraz zmianę stanu przycisku po wysłaniu zaproszenia.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultUser user = users.get(position);
        holder.tvName.setText(user.getUsername());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.ic_person)
                .into(holder.ivAvatar);

        holder.btnInvite.setOnClickListener(v -> {
            boolean inviteSent = listener.onInvite(user);

            if (inviteSent) {
                holder.btnInvite.setText("WYSŁANO");
                holder.btnInvite.setEnabled(false);
            }
        });
    }

    @Override
    public int getItemCount() { return users.size(); }

    /**
     * ViewHolder reprezentujący element listy urządzenia Bluetooth.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;
        MaterialButton btnInvite;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvDeviceName);
            ivAvatar = v.findViewById(R.id.ivDeviceAvatarContainer).findViewById(R.id.ivDeviceAvatar);
            btnInvite = v.findViewById(R.id.btnInviteDevice);
        }
    }
}
package com.example.cinematuz.ui.fragments.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.SearchResultUser;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(SearchResultUser user);
    }

    private final List<SearchResultUser> items = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public SuggestionAdapter(List<SearchResultUser> initialItems, OnSuggestionClickListener listener) {
        items.addAll(initialItems);
        this.listener = listener;
    }

    public void submitList(List<SearchResultUser> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        SearchResultUser user = items.get(position);
        holder.tvName.setText(user.getUsername());

        // --- Ładowanie awatara z zaokrągleniem za pomocą Glide ---
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .circleCrop() // To zamienia kwadrat w ładne kółko!
                    .into(holder.ivAvatar);
        } else {
            // Domyślna ikonka, jeśli użytkownik nie ma własnego zdjęcia
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }
        // ----------------------------------------------------------------

        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar; // Osobne miejsce na avatar

        SuggestionViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvSuggestionName);
            // Podpinamy ImageView z Twojego pliku XML
            ivAvatar = v.findViewById(R.id.ivSuggestionAvatar);
        }
    }
}
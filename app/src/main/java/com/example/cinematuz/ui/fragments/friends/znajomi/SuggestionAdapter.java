package com.example.cinematuz.ui.fragments.friends.znajomi;

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

/**
 * Adapter dla listy sugestii użytkowników podczas wyszukiwania nowych znajomych.
 * Odpowiada za wyświetlanie miniatur profilowych oraz nazw użytkowników pasujących do zapytania.
 */
public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder> {

    /**
     * Interfejs obsługujący kliknięcie w zasugerowanego użytkownika.
     */
    public interface OnSuggestionClickListener {
        /**
         * Wywoływane po kliknięciu w element sugestii.
         * @param user Obiekt wybranego użytkownika.
         */
        void onSuggestionClick(SearchResultUser user);
    }

    private final List<SearchResultUser> items = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    /**
     * Tworzy nową instancję adaptera sugestii.
     * 
     * @param initialItems Początkowa lista sugestii.
     * @param listener Listener kliknięć.
     */
    public SuggestionAdapter(List<SearchResultUser> initialItems, OnSuggestionClickListener listener) {
        items.addAll(initialItems);
        this.listener = listener;
    }

    /**
     * Aktualizuje listę sugestii i odświeża widok.
     * 
     * @param newItems Nowa lista wyników wyszukiwania.
     */
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

    /**
     * Wiąże dane użytkownika z widokiem sugestii. 
     * Ładuje i zaokrągla awatar użytkownika za pomocą biblioteki Glide.
     */
    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        SearchResultUser user = items.get(position);
        holder.tvName.setText(user.getUsername());

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getAvatarUrl())
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.ivAvatar);
        }

        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(user));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder dla elementu sugestii użytkownika.
     */
    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;

        SuggestionViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvSuggestionName);
            ivAvatar = v.findViewById(R.id.ivSuggestionAvatar);
        }
    }
}
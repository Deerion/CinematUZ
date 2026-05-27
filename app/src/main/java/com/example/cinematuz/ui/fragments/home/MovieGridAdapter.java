package com.example.cinematuz.ui.fragments.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cinematuz.R;
import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.ItemMovieGridBinding;
import com.example.cinematuz.ui.fragments.home.search.SearchResultAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter dla RecyclerView wyświetlający siatkę filmów i seriali na ekranie głównym oraz w bibliotece.
 * Obsługuje optymalne odświeżanie listy za pomocą DiffUtil oraz dwa źródła danych: API (MediaItem) i lokalną bazę (MovieEntity).
 */
public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
    private List<MediaItem> mediaItems = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    /**
     * Interfejs obsługujący kliknięcie w element siatki.
     */
    public interface OnItemClickListener { 
        void onItemClick(MediaItem item); 
    }

    /**
     * Interfejs obsługujący długie kliknięcie w element siatki.
     */
    public interface OnItemLongClickListener { 
        void onItemLongClick(MediaItem item, View anchorView); 
    }

    /**
     * Konstruktor adaptera.
     * 
     * @param listener Listener kliknięć.
     */
    public MovieGridAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Ustawia listener dla długich kliknięć.
     * 
     * @param longClickListener Obiekt implementujący OnItemLongClickListener.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    /**
     * Aktualizuje listę elementów pochodzących z API (TMDB).
     * Wykorzystuje DiffUtil do obliczenia różnic i animowanej aktualizacji widoku.
     * 
     * @param newList Nowa lista elementów mediów.
     */
    public void submitList(List<MediaItem> newList) {
        if (newList == null) return;
        if (newList == mediaItems) return;

        List<MediaItem> oldList = new ArrayList<>(mediaItems);
        List<MediaItem> nextList = new ArrayList<>(newList);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return nextList.size(); }
            @Override public boolean areItemsTheSame(int op, int np) { return oldList.get(op).getId() == nextList.get(np).getId(); }
            @Override public boolean areContentsTheSame(int op, int np) { return oldList.get(op).equals(nextList.get(np)); }
        });

        mediaItems = nextList;
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Aktualizuje listę na podstawie danych z lokalnej bazy danych Room.
     * Konwertuje obiekty MovieEntity na MediaItem.
     * 
     * @param newList Lista encji filmów z bazy danych.
     */
    public void updateList(List<MovieEntity> newList) {
        this.mediaItems.clear();
        for (MovieEntity entity : newList) {
            MediaItem item = new MediaItem();
            item.setId(entity.getId());
            item.setTitle(entity.getTitle());
            item.setPosterPath(entity.getPosterPath());
            item.setOverview(entity.getOverview());
            item.setVoteAverage(entity.getVoteAverage());
            item.setMediaType(entity.getMediaType());
            this.mediaItems.add(item);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MovieViewHolder(ItemMovieGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(mediaItems.get(position));
    }

    @Override
    public int getItemCount() { return mediaItems.size(); }

    /**
     * ViewHolder reprezentujący pojedynczy kafel filmu/serialu w siatce.
     */
    class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieGridBinding binding;
        
        public MovieViewHolder(ItemMovieGridBinding binding) { 
            super(binding.getRoot()); 
            this.binding = binding; 
        }

        /**
         * Wiąże dane MediaItem z widokami elementu listy.
         * Ładuje plakat, ustawia tytuł, ocenę, rok i gatunek.
         * 
         * @param item Obiekt danych mediów.
         */
        public void bind(MediaItem item) {
            binding.textTitle.setText(item.getTitle());
            binding.textRating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

            String year = (item.getReleaseDate() != null && item.getReleaseDate().length() >= 4) ? item.getReleaseDate().substring(0, 4) : "";
            binding.textSubtitle.setText(year);

            String mainGenre = SearchResultAdapter.getFirstGenreName(item.getGenreIds(), itemView.getContext());
            binding.textGenre.setText(mainGenre);

            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w342" + item.getPosterPath())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.hero_cinema)
                    .into(binding.imagePoster);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(item, itemView);
                    return true;
                }
                return false;
            });
        }
    }
}
package com.example.cinematuz.ui.fragments.home.search;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter dla wyników wyszukiwania filmów i seriali.
 * Obsługuje filtrowanie typów mediów (filmy, seriale, wszystkie) oraz tryb wyboru
 * pozwalający na dodawanie pozycji do grupy.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    /**
     * Typy filtrów dostępnych dla wyników wyszukiwania.
     */
    public enum FilterType { ALL, MOVIE, TV }

    private final List<MediaItem> allItems = new ArrayList<>();
    private final List<MediaItem> visibleItems = new ArrayList<>();
    private final OnItemClickListener listener;
    private final OnAddClickListener addListener;
    private FilterType currentFilter = FilterType.ALL;
    private boolean selectionMode = false;

    /**
     * Interfejs obsługujący kliknięcie w element wyniku wyszukiwania.
     */
    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    /**
     * Interfejs obsługujący kliknięcie przycisku "Dodaj" (widocznego w trybie wyboru).
     */
    public interface OnAddClickListener {
        void onAddClick(MediaItem item);
    }

    /**
     * Konstruktor adaptera.
     * 
     * @param listener Listener kliknięcia w element.
     * @param addListener Listener przycisku dodawania.
     */
    public SearchResultAdapter(OnItemClickListener listener, OnAddClickListener addListener) {
        this.listener = listener;
        this.addListener = addListener;
    }

    /**
     * Ustawia tryb wyboru, który steruje widocznością przycisku dodawania.
     * 
     * @param selectionMode Prawda, jeśli tryb wyboru ma być aktywny.
     */
    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    /**
     * Aktualizuje całą listę elementów i nakłada aktualny filtr.
     * 
     * @param newItems Nowa lista elementów z API.
     */
    public void submitList(List<MediaItem> newItems) {
        allItems.clear();
        if (newItems != null) {
            for (MediaItem item : newItems) {
                String mediaType = item.getMediaType();
                if ("movie".equals(mediaType) || "tv".equals(mediaType) || TextUtils.isEmpty(mediaType)) {
                    allItems.add(item);
                }
            }
        }
        applyFilter();
    }

    /**
     * Ustawia aktywny filtr typu mediów.
     * 
     * @param filter Typ filtra (ALL, MOVIE, TV).
     */
    public void setFilter(FilterType filter) {
        this.currentFilter = filter;
        applyFilter();
    }

    /**
     * Filtruje listę wszystkich elementów i aktualizuje listę widoczną.
     */
    private void applyFilter() {
        visibleItems.clear();
        for (MediaItem item : allItems) {
            if (currentFilter == FilterType.ALL) {
                visibleItems.add(item);
            } else if (currentFilter == FilterType.MOVIE && "movie".equals(item.getMediaType())) {
                visibleItems.add(item);
            } else if (currentFilter == FilterType.TV && "tv".equals(item.getMediaType())) {
                visibleItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(visibleItems.get(position), listener, addListener, selectionMode);
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    /**
     * Zwraca nazwę pierwszego gatunku z listy identyfikatorów.
     * 
     * @param genreIds Lista identyfikatorów gatunków.
     * @param context Kontekst do pobrania zasobów tekstowych.
     * @return Zlokalizowana nazwa gatunku lub pusty ciąg.
     */
    public static String getFirstGenreName(List<Integer> genreIds, Context context) {
        if (genreIds == null || genreIds.isEmpty()) return "";
        int id = genreIds.get(0);
        switch (id) {
            case 28: return context.getString(R.string.genre_action);
            case 12: return context.getString(R.string.genre_adventure);
            case 16: return context.getString(R.string.genre_animation);
            case 35: return context.getString(R.string.genre_comedy);
            case 80: return context.getString(R.string.genre_crime);
            case 99: return context.getString(R.string.genre_documentary);
            case 18: return context.getString(R.string.genre_drama);
            case 10751: return context.getString(R.string.genre_family);
            case 14: return context.getString(R.string.genre_fantasy);
            case 36: return context.getString(R.string.genre_history);
            case 27: return context.getString(R.string.genre_horror);
            case 10402: return context.getString(R.string.genre_music);
            case 9648: return context.getString(R.string.genre_mystery);
            case 10749: return context.getString(R.string.genre_romance);
            case 878: return context.getString(R.string.genre_scifi);
            case 10770: return context.getString(R.string.genre_movie);
            case 53: return context.getString(R.string.genre_thriller);
            case 10752: return context.getString(R.string.genre_war);
            case 37: return context.getString(R.string.genre_western);
            case 10759: return context.getString(R.string.genre_action_adventure);
            case 10762: return context.getString(R.string.genre_kids);
            case 10765: return context.getString(R.string.genre_scifi_fantasy);
            case 10768: return context.getString(R.string.genre_politics);
            default: return "";
        }
    }

    /**
     * ViewHolder dla elementu wyniku wyszukiwania.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPoster;
        TextView tvTitle, tvYear, tvRating, tvGenres;
        MaterialButton btnAdd;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivSearchResultPoster);
            tvTitle = itemView.findViewById(R.id.tvSearchResultTitle);
            tvYear = itemView.findViewById(R.id.tvSearchResultYear);
            tvRating = itemView.findViewById(R.id.tvSearchResultRating);
            tvGenres = itemView.findViewById(R.id.tvSearchResultGenres);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }

        /**
         * Wiąże dane MediaItem z widokiem.
         */
        void bind(MediaItem item, OnItemClickListener listener, OnAddClickListener addListener, boolean selectionMode) {
            tvTitle.setText(item.getTitle() != null ? item.getTitle() : "Brak tytułu");
            tvRating.setText(String.format("%.1f", item.getVoteAverage()));

            String date = item.getReleaseDate();
            if (date != null && date.length() >= 4) {
                tvYear.setText(date.substring(0, 4));
            } else {
                tvYear.setText("N/A");
            }

            Context context = itemView.getContext();
            String mediaTypeStr = "tv".equals(item.getMediaType()) ? context.getString(R.string.filter_tv) : context.getString(R.string.filter_movies);
            String firstGenre = SearchResultAdapter.getFirstGenreName(item.getGenreIds(), context);

            if (!firstGenre.isEmpty()) {
                tvGenres.setText(mediaTypeStr + " • " + firstGenre);
            } else {
                tvGenres.setText(mediaTypeStr);
            }

            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w185" + item.getPosterPath())
                    .placeholder(R.color.border_light)
                    .into(ivPoster);

            itemView.setOnClickListener(v -> listener.onItemClick(item));

            btnAdd.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
            btnAdd.setOnClickListener(v -> {
                if (addListener != null) {
                    addListener.onAddClick(item);
                }
            });
        }
    }
}
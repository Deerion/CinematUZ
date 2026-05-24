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

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
    private List<MediaItem> mediaItems = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener { void onItemClick(MediaItem item); }
    public interface OnItemLongClickListener { void onItemLongClick(MediaItem item, View anchorView); }

    public MovieGridAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // Metoda przyjmująca listę z API
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

    // Metoda przyjmująca listę z Bazy Danych (MovieEntity)
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

    class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieGridBinding binding;
        public MovieViewHolder(ItemMovieGridBinding binding) { super(binding.getRoot()); this.binding = binding; }

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
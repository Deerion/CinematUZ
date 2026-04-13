package com.example.cinematuz.ui.fragments.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cinematuz.R;
import com.example.cinematuz.databinding.ItemMovieGridBinding;
import com.example.cinematuz.data.models.MediaItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {
    private List<MediaItem> mediaItems = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(MediaItem item); }
    public MovieGridAdapter(OnItemClickListener listener) { this.listener = listener; }

    public void submitList(List<MediaItem> newList) {
        // Unikamy submitowania tej samej listy, co oszczędza procesor
        if (newList == mediaItems) return;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return mediaItems.size(); }
            @Override public int getNewListSize() { return newList.size(); }
            @Override public boolean areItemsTheSame(int op, int np) { return mediaItems.get(op).getId() == newList.get(np).getId(); }
            @Override public boolean areContentsTheSame(int op, int np) { return mediaItems.get(op).equals(newList.get(np)); }
        });
        this.mediaItems = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
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

            Glide.with(itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w342" + item.getPosterPath())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.hero_cinema)
                    .into(binding.imagePoster);

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
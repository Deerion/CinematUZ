package com.example.cinematuz.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem; // Upewnij się, że paczka jest zgodna z Twoją

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    private List<MediaItem> mediaItems = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MediaItem item);
    }

    public MovieGridAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MediaItem> items) {
        this.mediaItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_grid, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.bind(mediaItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mediaItems != null ? mediaItems.size() : 0;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePoster;
        TextView textRating;
        TextView textTitle;
        TextView textSubtitle;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePoster = itemView.findViewById(R.id.image_poster);
            textRating = itemView.findViewById(R.id.text_rating);
            textTitle = itemView.findViewById(R.id.text_title);
            textSubtitle = itemView.findViewById(R.id.text_subtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(mediaItems.get(position));
                }
            });
        }

        public void bind(MediaItem item) {
            Context context = itemView.getContext();

            // 1. Tytuł
            textTitle.setText(item.getTitle());

            // 2. Ocena (formatowana do 1 miejsca po przecinku)
            textRating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

            // 3. Wyciąganie roku z daty (np. "2023-10-15" -> "2023")
            String year = "";
            String releaseDate = item.getReleaseDate();
            if (releaseDate != null && releaseDate.length() >= 4) {
                year = releaseDate.substring(0, 4);
            }

            // 4. Gatunek (tutaj używamy typu media_type na start)
            String mediaTypeString;
            if ("tv".equals(item.getMediaType())) {
                mediaTypeString = context.getString(R.string.media_type_tv);
            } else {
                mediaTypeString = context.getString(R.string.media_type_movie);
            }

            textSubtitle.setText(context.getString(R.string.media_subtitle_format, mediaTypeString, year));

            // 5. Ładowanie plakatu z TMDB
            if (item.getPosterPath() != null && !item.getPosterPath().isEmpty()) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + item.getPosterPath();
                Glide.with(context)
                        .load(posterUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagePoster);
            } else {
                imagePoster.setImageResource(0); // Możesz dodać @drawable/ic_placeholder
            }
        }
    }
}
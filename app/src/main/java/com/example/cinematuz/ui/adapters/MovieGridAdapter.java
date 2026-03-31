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
import com.example.cinematuz.data.models.MediaItem;

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

            textTitle.setText(item.getTitle());

            textRating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

            String year = "";
            String releaseDate = item.getReleaseDate();
            if (releaseDate != null && releaseDate.length() >= 4) {
                year = releaseDate.substring(0, 4);
            }

            String genre = getGenreName(context, item.getGenreIds());

            textSubtitle.setText(String.format(Locale.getDefault(), "%s • %s", genre, year));

            // 5. Plakat
            if (item.getPosterPath() != null && !item.getPosterPath().isEmpty()) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + item.getPosterPath();
                Glide.with(context)
                        .load(posterUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagePoster);
            } else {
                imagePoster.setImageResource(0);
            }
        }

        // Pomocnicza metoda tłumacząca ID gatunku TMDB na polski string
        private String getGenreName(Context context, List<Integer> genreIds) {
            if (genreIds == null || genreIds.isEmpty()) return context.getString(R.string.genre_other);
            int id = genreIds.get(0);
            switch(id) {
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
                case 53: return context.getString(R.string.genre_thriller);
                case 10752: return context.getString(R.string.genre_war);
                case 37: return context.getString(R.string.genre_western);
                case 10759: return context.getString(R.string.genre_action_adventure);
                case 10762: return context.getString(R.string.genre_kids);
                case 10765: return context.getString(R.string.genre_scifi_fantasy);
                case 10768: return context.getString(R.string.genre_politics);
                default: return "Movie";
            }
        }
    }
}
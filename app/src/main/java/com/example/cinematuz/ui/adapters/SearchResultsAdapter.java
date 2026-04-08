package com.example.cinematuz.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ResultViewHolder> {

    public interface OnResultActionListener {
        void onItemClick(MediaItem item);
        void onAddClick(MediaItem item);
    }

    private final List<MediaItem> items = new ArrayList<>();
    private final OnResultActionListener listener;

    public SearchResultsAdapter(OnResultActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MediaItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoster;
        private final ImageButton btnAdd;
        private final TextView tvTitle;
        private final TextView tvGenre;
        private final TextView tvYear;
        private final TextView tvRating;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvYear = itemView.findViewById(R.id.tvYear);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        void bind(MediaItem item) {
            Context context = itemView.getContext();
            tvTitle.setText(item.getTitle() != null ? item.getTitle() : context.getString(R.string.genre_other));
            tvGenre.setText(resolveGenre(context, item.getGenreIds()));
            tvYear.setText(resolveYear(item.getReleaseDate()));
            tvRating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

            String posterPath = item.getPosterPath();
            if (posterPath != null && !posterPath.isEmpty()) {
                Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500" + posterPath)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivPoster);
            } else {
                ivPoster.setImageResource(R.drawable.ic_launcher_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });

            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClick(item);
                } else {
                    Toast.makeText(context, context.getString(R.string.cd_add_result), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private String resolveYear(String releaseDate) {
            if (releaseDate != null && releaseDate.length() >= 4) {
                return releaseDate.substring(0, 4);
            }
            return "-";
        }

        private String resolveGenre(Context context, List<Integer> genreIds) {
            if (genreIds == null || genreIds.isEmpty()) {
                return context.getString(R.string.genre_other);
            }

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
                case 53: return context.getString(R.string.genre_thriller);
                case 10752: return context.getString(R.string.genre_war);
                case 37: return context.getString(R.string.genre_western);
                case 10759: return context.getString(R.string.genre_action_adventure);
                case 10762: return context.getString(R.string.genre_kids);
                case 10765: return context.getString(R.string.genre_scifi_fantasy);
                case 10768: return context.getString(R.string.genre_politics);
                default: return context.getString(R.string.genre_other);
            }
        }
    }
}


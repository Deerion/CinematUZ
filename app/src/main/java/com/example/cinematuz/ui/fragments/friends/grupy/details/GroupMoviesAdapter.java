package com.example.cinematuz.ui.fragments.friends.grupy;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupMoviesAdapter extends RecyclerView.Adapter<GroupMoviesAdapter.ViewHolder> {

    private List<MediaItem> movies = new ArrayList<>();
    private Map<Integer, List<String>> votesMap; // Mapa: ID filmu -> lista UID głosujących
    private final OnMovieClickListener listener;
    private final OnVoteClickListener voteListener;

    public interface OnMovieClickListener {
        void onMovieClick(MediaItem item);
    }

    public interface OnVoteClickListener {
        void onVoteClick(MediaItem item, boolean isVoted);
    }

    public GroupMoviesAdapter(OnMovieClickListener listener, OnVoteClickListener voteListener) {
        this.listener = listener;
        this.voteListener = voteListener;
    }

    public void submitList(List<MediaItem> newList, Map<Integer, List<String>> votes) {
        this.movies = newList;
        this.votesMap = votes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_compact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = movies.get(position);
        holder.tvTitle.setText(item.getTitle());

        // Obsługa głosów
        String myUid = FirebaseAuth.getInstance().getUid();
        List<String> voters = (votesMap != null) ? votesMap.get(item.getId()) : new ArrayList<>();
        int count = (voters != null) ? voters.size() : 0;
        boolean amIVoting = voters != null && voters.contains(myUid);

        holder.tvVoteCount.setText(String.valueOf(count));

        // Zmiana ikony w zależności od tego, czy użytkownik zagłosował
        if (amIVoting) {
            holder.ivVote.setImageResource(R.drawable.ic_favorite); // pełne serce
        } else {
            holder.ivVote.setImageResource(R.drawable.ic_favorite_outline); // puste serce
        }

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w342" + item.getPosterPath())
                .placeholder(R.drawable.hero_cinema)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> listener.onMovieClick(item));

        holder.ivVote.setOnClickListener(v -> {
            if (voteListener != null) {
                voteListener.onVoteClick(item, amIVoting);
            }
        });
    }

    @Override
    public int getItemCount() { return movies.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivPoster;
        TextView tvTitle, tvVoteCount;
        ImageView ivVote;

        ViewHolder(View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivCompactPoster);
            tvTitle = itemView.findViewById(R.id.tvCompactTitle);
            tvVoteCount = itemView.findViewById(R.id.tvVoteCount);
            ivVote = itemView.findViewById(R.id.ivVote);
        }
    }
}
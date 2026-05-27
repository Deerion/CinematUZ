package com.example.cinematuz.ui.fragments.friends.grupy.details;

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

/**
 * Adapter dla kompaktowej listy filmów wewnątrz grupy.
 * Wyświetla miniatury plakatów, tytuły oraz umożliwia oddawanie głosów na propozycje.
 */
public class GroupMoviesAdapter extends RecyclerView.Adapter<GroupMoviesAdapter.ViewHolder> {

    private List<MediaItem> movies = new ArrayList<>();
    private Map<Integer, List<String>> votesMap;
    private final OnMovieClickListener listener;
    private final OnVoteClickListener voteListener;
    private final OnMovieLongClickListener longClickListener;

    /**
     * Interfejs obsługujący kliknięcie w element filmu.
     */
    public interface OnMovieClickListener {
        void onMovieClick(MediaItem item);
    }

    /**
     * Interfejs obsługujący kliknięcie w ikonę głosowania.
     */
    public interface OnVoteClickListener {
        /**
         * Wywoływane przy kliknięciu w przycisk głosu.
         * @param item Film, na który oddawany jest głos.
         * @param isVoted Czy użytkownik już wcześniej głosował na ten film.
         */
        void onVoteClick(MediaItem item, boolean isVoted);
    }

    /**
     * Interfejs dla obsługi długiego kliknięcia na element filmu.
     */
    public interface OnMovieLongClickListener {
        void onMovieLongClick(MediaItem item);
    }

    /**
     * Konstruktor adaptera.
     * 
     * @param listener Listener kliknięcia.
     * @param voteListener Listener głosowania.
     * @param longClickListener Listener długiego kliknięcia.
     */
    public GroupMoviesAdapter(OnMovieClickListener listener,
                              OnVoteClickListener voteListener,
                              OnMovieLongClickListener longClickListener) {
        this.listener = listener;
        this.voteListener = voteListener;
        this.longClickListener = longClickListener;
    }

    /**
     * Aktualizuje listę filmów oraz mapę głosów.
     * 
     * @param newList Nowa lista filmów.
     * @param votes Mapa głosów (ID filmu -> lista UID).
     */
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

    /**
     * Wiąże dane filmu z widokiem. Ustawia tytuł, liczbę głosów, ikonę ulubionych oraz plakaty.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem item = movies.get(position);
        holder.tvTitle.setText(item.getTitle());

        String myUid = FirebaseAuth.getInstance().getUid();
        List<String> voters = (votesMap != null) ? votesMap.get(item.getId()) : new ArrayList<>();
        int count = (voters != null) ? voters.size() : 0;
        boolean amIVoting = voters != null && voters.contains(myUid);

        holder.tvVoteCount.setText(String.valueOf(count));

        if (amIVoting) {
            holder.ivVote.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.ivVote.setImageResource(R.drawable.ic_favorite_outline);
        }

        Glide.with(holder.itemView.getContext())
                .load("https://image.tmdb.org/t/p/w342" + item.getPosterPath())
                .placeholder(R.drawable.hero_cinema)
                .into(holder.ivPoster);

        holder.itemView.setOnClickListener(v -> listener.onMovieClick(item));

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMovieLongClick(item);
                return true;
            }
            return false;
        });

        holder.ivVote.setOnClickListener(v -> {
            if (voteListener != null) {
                voteListener.onVoteClick(item, amIVoting);
            }
        });
    }

    @Override
    public int getItemCount() { return movies.size(); }

    /**
     * ViewHolder dla elementu kompaktowej listy filmów.
     */
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
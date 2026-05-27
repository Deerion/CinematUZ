package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.Friend;
import com.example.cinematuz.data.models.MediaItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zaawansowany adapter dla listy filmów wewnątrz grupy.
 * Wyświetla listę filmów wraz z postępem głosowania, awatarami głosujących osób
 * oraz umożliwia oddawanie głosów i usuwanie filmów z propozycji grupowych.
 */
public class AdvancedGroupMoviesAdapter extends RecyclerView.Adapter<AdvancedGroupMoviesAdapter.ViewHolder> {
    private List<MediaItem> movies = new ArrayList<>();
    private Map<Integer, List<String>> votes = new HashMap<>();
    private List<Friend> groupMembers = new ArrayList<>();
    private int maxMembers = 1;
    private final OnMovieInteractionListener listener;

    /**
     * Interfejs do obsługi interakcji z filmami na liście grupowej.
     */
    public interface OnMovieInteractionListener {
        /**
         * Przełącza głos użytkownika na dany film.
         * 
         * @param movie Film, na który oddawany/wycofywany jest głos.
         * @param currentlyVoted Czy użytkownik aktualnie ma oddany głos na ten film.
         */
        void onToggleVote(MediaItem movie, boolean currentlyVoted);

        /**
         * Usuwa film z listy propozycji grupowych.
         * 
         * @param movie Film do usunięcia.
         */
        void onDeleteMovie(MediaItem movie);
    }

    /**
     * Konstruktor adaptera.
     * 
     * @param listener Listener interakcji z filmami.
     */
    public AdvancedGroupMoviesAdapter(OnMovieInteractionListener listener) {
        this.listener = listener;
    }

    /**
     * Aktualizuje dane w adapterze i odświeża widok.
     * 
     * @param newMovies Nowa lista filmów.
     * @param newVotes Mapa głosów (ID filmu -> lista UID głosujących).
     * @param members Lista członków grupy (do pobrania awatarów).
     * @param maxMembers Całkowita liczba członków grupy (do paska postępu).
     */
    public void setGroupData(List<MediaItem> newMovies, Map<Integer, List<String>> newVotes, List<Friend> members, int maxMembers) {
        this.movies = new ArrayList<>(newMovies);
        this.votes = new HashMap<>(newVotes);
        this.groupMembers = members;
        this.maxMembers = maxMembers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_movie, parent, false));
    }

    /**
     * Wiąże dane filmu z widokiem. Ustawia tytuł, rok, gatunek, pasek postępu głosów oraz awatary głosujących.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaItem movie = movies.get(position);
        String myUid = FirebaseAuth.getInstance().getUid();
        List<String> voters = votes.getOrDefault(movie.getId(), new ArrayList<>());
        boolean iVoted = voters.contains(myUid);

        holder.tvTitle.setText(movie.getTitle() != null ? movie.getTitle() : "Brak tytułu");

        String date = movie.getReleaseDate();
        if (date != null && date.length() >= 4) {
            holder.tvYear.setText(date.substring(0, 4));
        } else {
            holder.tvYear.setText("");
        }

        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            holder.tvGenre.setText("• " + movie.getGenres().get(0).getName());
        } else {
            holder.tvGenre.setText("");
        }

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load("https://image.tmdb.org/t/p/w200" + movie.getPosterPath())
                    .into(holder.ivPoster);
        }

        holder.tvVoteCount.setText(voters.size() + " głosów");
        holder.progressBar.setMax(maxMembers);
        holder.progressBar.setProgress(voters.size(), true);

        holder.btnVote.setImageResource(iVoted ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline);
        holder.btnVote.setColorFilter(iVoted ? 0xFFFF0000 : 0xFF757575);

        holder.btnVote.setOnClickListener(v -> {
            if (listener != null) listener.onToggleVote(movie, iVoted);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteMovie(movie);
        });

        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (iVoted) {
            card.setStrokeColor(android.graphics.Color.RED);
            card.setStrokeWidth(4);
        } else {
            card.setStrokeWidth(0);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", movie);
            Navigation.findNavController(v).navigate(R.id.detailsFragment, bundle);
        });

        holder.ivVoter1.setVisibility(View.GONE);
        holder.ivVoter2.setVisibility(View.GONE);
        holder.ivVoter3.setVisibility(View.GONE);
        holder.tvExtraVoters.setVisibility(View.GONE);

        ImageView[] avatarViews = {holder.ivVoter1, holder.ivVoter2, holder.ivVoter3};
        int drawnAvatars = 0;

        for (String uid : voters) {
            if (drawnAvatars >= 3) break;
            String avatarUrl = null;
            for (Friend f : groupMembers) {
                if (f.getId().equals(uid)) {
                    avatarUrl = f.getAvatarUrl();
                    break;
                }
            }
            avatarViews[drawnAvatars].setVisibility(View.VISIBLE);
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .into(avatarViews[drawnAvatars]);
            } else {
                avatarViews[drawnAvatars].setImageResource(R.drawable.ic_person);
            }
            drawnAvatars++;
        }

        if (voters.size() > 3) {
            holder.tvExtraVoters.setVisibility(View.VISIBLE);
            holder.tvExtraVoters.setText("+" + (voters.size() - 3));
        }
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    /**
     * ViewHolder dla elementu filmu grupowego.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster, ivVoter1, ivVoter2, ivVoter3, btnVote, btnDelete;
        TextView tvTitle, tvYear, tvGenre, tvVoteCount, tvExtraVoters;
        LinearProgressIndicator progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvYear = itemView.findViewById(R.id.tvMovieYear);
            tvGenre = itemView.findViewById(R.id.tvMovieGenre);
            tvVoteCount = itemView.findViewById(R.id.tvVoteCount);
            tvExtraVoters = itemView.findViewById(R.id.tvExtraVoters);
            ivVoter1 = itemView.findViewById(R.id.ivVoter1);
            ivVoter2 = itemView.findViewById(R.id.ivVoter2);
            ivVoter3 = itemView.findViewById(R.id.ivVoter3);
            progressBar = itemView.findViewById(R.id.progressVotes);
            btnVote = itemView.findViewById(R.id.btnVote);
            btnDelete = itemView.findViewById(R.id.btnOptions);
        }
    }
}
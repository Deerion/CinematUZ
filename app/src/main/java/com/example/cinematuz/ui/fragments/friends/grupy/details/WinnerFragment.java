package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.ui.fragments.home.search.SearchResultAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WinnerFragment extends Fragment {

    private MediaItem winnerMovie;
    private String winnerReason;
    private String groupId;
    private String winnerId;
    private ArrayList<MediaItem> eligibleMovies;
    private boolean isAdmin;

    private TextView tvWinnerTitle;
    private TextView tvWinnerHeader;
    private TextView tvWinnerDetails;
    private ImageView ivWinnerPoster;
    private com.example.cinematuz.ui.fragments.home.details.DetailsViewModel detailsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            winnerMovie = (MediaItem) getArguments().getSerializable("WINNER_MOVIE");
            winnerId = getArguments().getString("WINNER_ID");
            groupId = getArguments().getString("GROUP_ID");
            winnerId = getArguments().getString("WINNER_ID");
            isAdmin = getArguments().getBoolean("IS_ADMIN", false);
            if (getArguments().containsKey("ELIGIBLE_MOVIES")) {
                eligibleMovies = (ArrayList<MediaItem>) getArguments().getSerializable("ELIGIBLE_MOVIES");
            }
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToGroup();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_winner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ukrywamy dolną nawigację na tym ekranie
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) navView.setVisibility(View.GONE);

        tvWinnerTitle = view.findViewById(R.id.tvWinnerTitle);
        tvWinnerHeader = view.findViewById(R.id.tvWinnerHeader);
        tvWinnerDetails = view.findViewById(R.id.tvWinnerDetails);
        ivWinnerPoster = view.findViewById(R.id.ivWinnerPoster);

        View btnBack = view.findViewById(R.id.btnWinnerBack);
        View btnClose = view.findViewById(R.id.btnWinnerClose);
        View btnReroll = view.findViewById(R.id.btnWinnerReroll);
        View btnSeeDetails = view.findViewById(R.id.btnWinnerSeeDetails);

        if (btnBack != null) btnBack.setOnClickListener(v -> navigateBackToGroup());
        if (btnClose != null) btnClose.setOnClickListener(v -> navigateBackToGroup());
        if (btnReroll != null) {
            btnReroll.setVisibility(View.VISIBLE); // Wszystkie przyciski zawsze widoczne
            btnReroll.setOnClickListener(v -> performReroll());
        }
        if (btnSeeDetails != null) {
            btnSeeDetails.setOnClickListener(v -> {
                if (winnerMovie != null) {
                    Bundle b = new Bundle();
                    b.putInt("mediaId", winnerMovie.getId());
                    b.putString("mediaType", "movie"); // Winner is always movie in this context
                    Navigation.findNavController(view).navigate(R.id.detailsFragment, b);
                }
            });
        }

        detailsViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.example.cinematuz.ui.fragments.home.details.DetailsViewModel.class);

        detailsViewModel.fullDetails.observe(getViewLifecycleOwner(), fullMovie -> {
            if (fullMovie != null) {
                this.winnerMovie = fullMovie;
                bindWinnerUi();
            }
        });

        if (winnerMovie != null) {
            // Pobieramy pełne dane (gatunki, rok), jeśli ich brakuje
            detailsViewModel.loadData(winnerMovie.getId(), "movie", "pl");
            bindWinnerUi();
        } else if (winnerId != null) {
            // Dopiero jeśli brak obiektu, dociągamy go
            fetchWinnerMovieIfNeeded();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Przywracamy dolną nawigację przy wychodzeniu z fragmentu
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) navView.setVisibility(View.VISIBLE);
    }

    private void bindWinnerUi() {
        // Sprawdzenie czy mamy jakiekolwiek dane
        if (winnerMovie == null) {
            if (tvWinnerTitle != null) tvWinnerTitle.setText("Brak danych o zwycięzcy");
            if (tvWinnerDetails != null) tvWinnerDetails.setText("Spróbuj ponownie później");
            return;
        }

        // Tytuł
        if (tvWinnerTitle != null) {
            tvWinnerTitle.setText(winnerMovie.getTitle() != null ? winnerMovie.getTitle() : "Brak tytułu");
        }

        // Detale (rok • gatunki) - tutaj sprawdzamy czy buildDetailsText zwraca pusty ciąg
        if (tvWinnerDetails != null) {
            String details = buildDetailsText(winnerMovie);
            tvWinnerDetails.setText(!details.isEmpty() ? details : "Brak informacji");
        }

        // Plakat
        if (ivWinnerPoster != null && winnerMovie.getPosterPath() != null) {
            String imageUrl = "https://image.tmdb.org/t/p/w500" + winnerMovie.getPosterPath();
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.hero_cinema)
                    .error(R.drawable.hero_cinema)
                    .centerCrop()
                    .into(ivWinnerPoster);
        }
    }

    private String buildDetailsText(MediaItem movie) {
        List<String> parts = new ArrayList<>();

        // Rok
        String date = movie.getReleaseDate();
        if (date != null && date.length() >= 4) {
            parts.add(date.substring(0, 4));
        }

        // Gatunki
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            for (int i = 0; i < Math.min(movie.getGenres().size(), 2); i++) {
                parts.add(movie.getGenres().get(i).getName());
            }
        } else if (movie.getGenreIds() != null && !movie.getGenreIds().isEmpty()) {
            String g = SearchResultAdapter.getFirstGenreName(movie.getGenreIds(), getContext());
            if (g != null && !g.isEmpty()) parts.add(g);
        }

        return parts.isEmpty() ? "" : android.text.TextUtils.join(" • ", parts);
    }

    private void fetchWinnerMovieIfNeeded() {
        if (groupId == null || winnerId == null || winnerId.trim().isEmpty()) return;

        FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("movies")
                .document(winnerId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || !doc.exists()) return;
                    MediaItem loaded = doc.toObject(MediaItem.class);
                    if (loaded != null) {
                        if (doc.contains("tmdbId") && doc.getLong("tmdbId") != null) {
                            loaded.setId(doc.getLong("tmdbId").intValue());
                        }
                        mergeWinnerDetailsFromEligibleMovies(loaded);
                        winnerMovie = loaded;
                        bindWinnerUi();
                    }
                });
    }

    private void performReroll() {
        if (eligibleMovies == null || eligibleMovies.isEmpty()) {
            if (groupId == null) return;
            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .collection("movies")
                    .get()
                    .addOnSuccessListener(snaps -> {
                        eligibleMovies = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snaps) {
                            MediaItem item = doc.toObject(MediaItem.class);
                            if (doc.contains("tmdbId") && doc.getLong("tmdbId") != null) {
                                item.setId(doc.getLong("tmdbId").intValue());
                            }
                            eligibleMovies.add(item);
                        }
                        rerollWithCandidates();
                    });
            return;
        }
        rerollWithCandidates();
    }

    private void rerollWithCandidates() {
        MediaItem nextWinner = pickDifferentWinner();
        if (nextWinner == null) return;

        winnerMovie = nextWinner;
        winnerReason = "Decyzja losu";
        bindWinnerUi();

        if (groupId != null) {
            FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .update("winnerId", String.valueOf(nextWinner.getId()), "winnerReason", winnerReason);
        }
    }

    private MediaItem pickDifferentWinner() {
        if (eligibleMovies == null || eligibleMovies.isEmpty()) return null;
        List<MediaItem> candidates = new ArrayList<>();
        int currentId = winnerMovie != null ? winnerMovie.getId() : -1;
        for (MediaItem movie : eligibleMovies) {
            if (movie != null && movie.getId() != currentId) {
                candidates.add(movie);
            }
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(new Random().nextInt(candidates.size()));
    }

    private void mergeWinnerDetailsFromEligibleMovies(MediaItem loaded) {
        if (eligibleMovies == null || loaded == null) return;
        for (MediaItem candidate : eligibleMovies) {
            if (candidate != null && candidate.getId() == loaded.getId()) {
                if ((loaded.getReleaseDate() == null || loaded.getReleaseDate().isEmpty()) && candidate.getReleaseDate() != null) {
                    loaded.setReleaseDate(candidate.getReleaseDate());
                }
                if ((loaded.getGenres() == null || loaded.getGenres().isEmpty()) && candidate.getGenres() != null) {
                    loaded.setGenres(candidate.getGenres());
                }
                if ((loaded.getGenreIds() == null || loaded.getGenreIds().isEmpty()) && candidate.getGenreIds() != null) {
                    loaded.setGenreIds(candidate.getGenreIds());
                }
                if ((loaded.getPosterPath() == null || loaded.getPosterPath().isEmpty()) && candidate.getPosterPath() != null) {
                    loaded.setPosterPath(candidate.getPosterPath());
                }
                break;
            }
        }
    }

    private void navigateBackToGroup() {
        if (getView() != null) {
            NavController navController = Navigation.findNavController(requireView());
            boolean popped = navController.popBackStack(R.id.groupDetailsFragment, false);
            if (!popped) {
                navController.navigateUp();
            }
        }
    }
}

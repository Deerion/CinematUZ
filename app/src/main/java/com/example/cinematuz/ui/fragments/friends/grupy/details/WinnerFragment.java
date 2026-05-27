package com.example.cinematuz.ui.fragments.friends.grupy.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Vibrator;
import android.content.Context;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment wyświetlający zwycięski film wybrany przez grupę lub wylosowany przez użytkownika.
 * Obsługuje wibrację przy ogłoszeniu wyniku, wyświetla plakaty oraz umożliwia ponowne losowanie
 * lub przejście do szczegółów filmu.
 */
public class WinnerFragment extends Fragment {

    private MediaItem winnerMovie;
    private String winnerReason;
    private String groupId;
    private String winnerId;
    private ArrayList<MediaItem> eligibleMovies;
    private boolean isAdmin;
    private boolean hasVibrated = false;
    private ListenerRegistration groupListener;

    private TextView tvWinnerTitle;
    private TextView tvWinnerHeader;
    private TextView tvWinnerDetails;
    private ImageView ivWinnerPoster;
    private com.example.cinematuz.ui.fragments.home.details.DetailsViewModel detailsViewModel;

    /**
     * Inicjalizuje dane zwycięzcy przekazane w argumentach i konfiguruje obsługę przycisku wstecz.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            winnerMovie = (MediaItem) getArguments().getSerializable("WINNER_MOVIE");
            winnerId = getArguments().getString("WINNER_ID");
            groupId = getArguments().getString("GROUP_ID");
            isAdmin = getArguments().getBoolean("IS_ADMIN", false);
            winnerReason = getArguments().getString("WINNER_REASON", "Głosowanie grupowe");
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

    /**
     * Inicjalizuje widoki, nasłuchiwacze i ViewModel. Uruchamia wibrację oraz nasłuchiwanie zmian w grupie.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
            if (groupId != null && !groupId.isEmpty() && !isAdmin) {
                btnReroll.setVisibility(View.GONE);
            } else {
                btnReroll.setVisibility(View.VISIBLE);
                btnReroll.setOnClickListener(v -> navigateToShake());
            }
        }

        if (btnSeeDetails != null) {
            btnSeeDetails.setOnClickListener(v -> {
                if (winnerMovie != null) {
                    Bundle b = new Bundle();
                    b.putSerializable("MEDIA_ITEM", winnerMovie);
                    b.putInt("mediaId", winnerMovie.getId());
                    b.putString("mediaType", "movie");
                    Navigation.findNavController(view).navigate(R.id.detailsFragment, b);
                }
            });
        }

        detailsViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.example.cinematuz.ui.fragments.home.details.DetailsViewModel.class);

        if (!hasVibrated) {
            triggerWinnerVibration();
            hasVibrated = true;
        }

        detailsViewModel.fullDetails.observe(getViewLifecycleOwner(), fullMovie -> {
            if (fullMovie != null) {
                this.winnerMovie = fullMovie;
                bindWinnerUi();
            }
        });

        if (winnerMovie != null) {
            detailsViewModel.loadData(winnerMovie.getId(), "movie", "pl");
            bindWinnerUi();
        } else if (winnerId != null) {
            fetchWinnerMovieIfNeeded();
        }

        if (groupId != null && !groupId.isEmpty()) {
            groupListener = FirebaseFirestore.getInstance()
                    .collection("groups")
                    .document(groupId)
                    .addSnapshotListener((snapshot, error) -> {
                        if (error != null || snapshot == null || !snapshot.exists()) return;

                        String firebaseWinnerId = snapshot.getString("winnerId");
                        String firebaseWinnerReason = snapshot.getString("winnerReason");

                        if (firebaseWinnerId != null && !firebaseWinnerId.isEmpty() &&
                                (winnerMovie == null || !String.valueOf(winnerMovie.getId()).equals(firebaseWinnerId))) {

                            winnerId = firebaseWinnerId;
                            winnerReason = firebaseWinnerReason != null ? firebaseWinnerReason : "Decyzja losu";

                            GroupDetailsFragment.lastSeenWinnerId = firebaseWinnerId;

                            triggerWinnerVibration();
                            fetchWinnerMovieIfNeeded();

                            try {
                                int tmdbId = Integer.parseInt(firebaseWinnerId);
                                detailsViewModel.loadData(tmdbId, "movie", "pl");
                            } catch (NumberFormatException ignored) {}
                        }
                    });
        }
    }

    /**
     * Przywraca widoczność nawigacji dolnej i usuwa nasłuchiwacze przy niszczeniu widoku.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (groupListener != null) {
            groupListener.remove();
        }
        View navView = requireActivity().findViewById(R.id.nav_view);
        if (navView != null) navView.setVisibility(View.VISIBLE);
    }

    /**
     * Wypełnia elementy UI danymi zwycięskiego filmu.
     */
    private void bindWinnerUi() {
        if (winnerMovie == null) {
            if (tvWinnerTitle != null) tvWinnerTitle.setText("Brak danych o zwycięzcy");
            if (tvWinnerDetails != null) tvWinnerDetails.setText("Spróbuj ponownie później");
            return;
        }

        if (tvWinnerTitle != null) {
            tvWinnerTitle.setText(winnerMovie.getTitle() != null ? winnerMovie.getTitle() : "Brak tytułu");
        }

        if (tvWinnerHeader != null && winnerReason != null) {
            tvWinnerHeader.setText(winnerReason);
        }

        if (tvWinnerDetails != null) {
            String details = buildDetailsText(winnerMovie);
            tvWinnerDetails.setText(!details.isEmpty() ? details : "Brak informacji");
        }

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

    /**
     * Buduje tekst z dodatkowymi informacjami o filmie (rok, gatunki).
     * 
     * @param movie Obiekt filmu.
     * @return Sformatowany tekst szczegółów.
     */
    private String buildDetailsText(MediaItem movie) {
        List<String> parts = new ArrayList<>();

        String date = movie.getReleaseDate();
        if (date != null && date.length() >= 4) {
            parts.add(date.substring(0, 4));
        }

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

    /**
     * Pobiera brakujące dane o filmie z kolekcji propozycji grupy w Firestore.
     */
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

    /**
     * Przechodzi do ekranu losowania (potrząsania), pobierając wcześniej listę filmów, jeśli to konieczne.
     */
    private void navigateToShake() {
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
                        proceedToShake();
                    });
            return;
        }
        proceedToShake();
    }

    /**
     * Wykonuje faktyczną nawigację do fragmentu ShakeFragment.
     */
    private void proceedToShake() {
        if (eligibleMovies == null || eligibleMovies.isEmpty()) {
            if (getView() != null) {
                android.widget.Toast.makeText(requireContext(), "Brak filmów do wylosowania!", android.widget.Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Bundle b = new Bundle();
        b.putSerializable("ELIGIBLE_MOVIES", eligibleMovies);
        if (groupId != null) {
            b.putString("GROUP_ID", groupId);
        }

        if (getView() != null) {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack(R.id.winnerFragment, true);
            navController.navigate(R.id.shakeFragment, b);
        }
    }

    /**
     * Uzupełnia brakujące szczegóły wczytanego filmu na podstawie listy dostępnych propozycji.
     */
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

    /**
     * Uruchamia krótką wibrację urządzenia.
     */
    private void triggerWinnerVibration() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(500);
        }
    }

    /**
     * Wraca do ekranu szczegółów grupy.
     */
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
package com.example.cinematuz.ui.fragments.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.repositories.MovieRepository;

import java.util.List;

/**
 * ViewModel dla ekranu biblioteki użytkownika.
 * Zarządza pobieraniem filmów o różnych statusach (do obejrzenia, obejrzane) z lokalnej bazy danych Room
 * oraz synchronizuje zmiany ze statystykami w Firebase.
 */
public class LibraryViewModel extends AndroidViewModel {

    private final MovieRepository repository;

    /**
     * Inicjalizuje ViewModel i repozytorium.
     * 
     * @param application Kontekst aplikacji.
     */
    public LibraryViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
    }

    /**
     * Pobiera listę filmów i seriali oznaczonych jako "Do obejrzenia".
     * 
     * @return LiveData zawierająca listę encji filmów.
     */
    public LiveData<List<MovieEntity>> getMoviesToWatch() {
        return repository.getMoviesByWatchStatus(false);
    }

    /**
     * Pobiera listę filmów i seriali oznaczonych jako "Obejrzane".
     * 
     * @return LiveData zawierająca listę encji filmów.
     */
    public LiveData<List<MovieEntity>> getWatchedMovies() {
        return repository.getMoviesByWatchStatus(true);
    }

    /**
     * Usuwa wybrany element z biblioteki i aktualizuje statystyki w Firebase.
     * 
     * @param item Obiekt mediów do usunięcia.
     */
    public void removeFromLibrary(MediaItem item) {
        repository.deleteMovieById(item.getId());
        updateFirebaseStats(item, false);
    }

    /**
     * Aktualizuje liczniki w profilu użytkownika w Firestore po usunięciu lub dodaniu do biblioteki.
     * 
     * @param item Obiekt mediów.
     * @param isMarkingAsWatched Czy element jest oznaczany jako obejrzany.
     */
    private void updateFirebaseStats(MediaItem item, boolean isMarkingAsWatched) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        com.google.firebase.firestore.DocumentReference profileRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("profiles").document(uid);
        String fieldToUpdate = "tv".equalsIgnoreCase(item.getMediaType()) ? "stats.tvShowsWatched" : "stats.moviesWatched";

        if (isMarkingAsWatched) {
            profileRef.update(fieldToUpdate, com.google.firebase.firestore.FieldValue.increment(1));
        } else {
            profileRef.update(fieldToUpdate, com.google.firebase.firestore.FieldValue.increment(-1));
        }
    }

    /**
     * Konwertuje obiekt encji bazy danych na model danych używany w interfejsie.
     * 
     * @param entity Encja z bazy Room.
     * @return Obiekt MediaItem.
     */
    public MediaItem convertToMediaItem(MovieEntity entity) {
        MediaItem item = new MediaItem();
        item.setId(entity.getId());
        item.setTitle(entity.getTitle());
        item.setPosterPath(entity.getPosterPath());
        item.setOverview(entity.getOverview());
        item.setVoteAverage(entity.getVoteAverage());
        item.setMediaType(entity.getMediaType());
        return item;
    }
}
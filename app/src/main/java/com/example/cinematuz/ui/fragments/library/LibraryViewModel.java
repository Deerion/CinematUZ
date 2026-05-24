package com.example.cinematuz.ui.fragments.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.repositories.MovieRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    private final MovieRepository repository;

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
    }

    // Pobiera filmy ze statusem "Do Obejrzenia"
    public LiveData<List<MovieEntity>> getMoviesToWatch() {
        return repository.getMoviesByWatchStatus(false);
    }

    // Pobiera filmy ze statusem "Obejrzane"
    public LiveData<List<MovieEntity>> getWatchedMovies() {
        return repository.getMoviesByWatchStatus(true);
    }

    // Usuwanie filmu z biblioteki
    public void removeFromLibrary(MediaItem item) {
        repository.deleteMovieById(item.getId());
        updateFirebaseStats(item, false);
    }

    private void updateFirebaseStats(MediaItem item, boolean isMarkingAsWatched) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        com.google.firebase.firestore.DocumentReference profileRef = com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("profiles").document(uid);
        String fieldToUpdate = "tv".equalsIgnoreCase(item.getMediaType()) ? "stats.tvShowsWatched" : "stats.moviesWatched";

        if (isMarkingAsWatched) {
            profileRef.update(fieldToUpdate, com.google.firebase.firestore.FieldValue.increment(1));
        } else {
            // Przy usuwaniu z biblioteki, musimy wiedzieć czy był obejrzany, 
            // ale w tym ViewModelu zakładamy, że jeśli usuwamy coś co jest w watchedList, to zmniejszamy.
            // LibraryFragment wywołuje to tylko przy usuwaniu.
            profileRef.update(fieldToUpdate, com.google.firebase.firestore.FieldValue.increment(-1));
        }
    }

    // Konwersja formatu bazy danych (MovieEntity) na format API (MediaItem)
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

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
    public void removeFromLibrary(int movieId) {
        repository.deleteMovieById(movieId);
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

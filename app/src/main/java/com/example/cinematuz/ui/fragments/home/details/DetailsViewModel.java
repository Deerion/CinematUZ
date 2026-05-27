package com.example.cinematuz.ui.fragments.home.details;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.repositories.MovieRepository;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.Cast;
import com.example.cinematuz.data.models.CreditsResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel dla ekranu szczegółów filmu lub serialu.
 * Odpowiada za pobieranie detali, obsady i trailerów z API oraz synchronizację
 * stanu biblioteki z lokalną bazą danych i statystykami w Firebase.
 */
public class DetailsViewModel extends AndroidViewModel {

    private final MovieRepository repository;

    private final MutableLiveData<MediaItem> _fullDetails = new MutableLiveData<>();
    /** Pełne szczegóły filmu/serialu pobrane z API. */
    public LiveData<MediaItem> fullDetails = _fullDetails;

    private final MutableLiveData<List<Cast>> _cast = new MutableLiveData<>();
    /** Lista członków obsady. */
    public LiveData<List<Cast>> cast = _cast;

    private final MutableLiveData<String> _trailerKey = new MutableLiveData<>();
    /** Klucz wideo YouTube dla trailera. */
    public LiveData<String> trailerKey = _trailerKey;

    private final MutableLiveData<MovieEntity> _localMovieState = new MutableLiveData<>();
    /** Stan filmu w lokalnej bazie danych (czy jest w bibliotece/obejrzany). */
    public LiveData<MovieEntity> localMovieState = _localMovieState;

    /**
     * Inicjalizuje ViewModel i repozytorium.
     * 
     * @param application Kontekst aplikacji.
     */
    public DetailsViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
    }

    /**
     * Ładuje szczegółowe informacje oraz obsadę dla danego elementu mediów.
     * 
     * @param mediaId Identyfikator elementu.
     * @param mediaType Typ mediów ("movie" lub "tv").
     * @param lang Kod języka.
     */
    public void loadData(int mediaId, String mediaType, String lang) {
        repository.getDetails(mediaId, mediaType, lang, new Callback<MediaItem>() {
            @Override
            public void onResponse(Call<MediaItem> call, Response<MediaItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MediaItem details = response.body();
                    details.setMediaType(mediaType);
                    _fullDetails.setValue(details);
                }
            }
            @Override
            public void onFailure(Call<MediaItem> call, Throwable t) {}
        });

        repository.getCredits(mediaId, mediaType, lang, new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _cast.setValue(response.body().getCast());
                }
            }
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {}
        });
    }

    /**
     * Pobiera klucz trailera z serwisu YouTube dla danego filmu/serialu.
     * 
     * @param mediaId Identyfikator elementu.
     * @param mediaType Typ mediów.
     */
    public void fetchTrailer(int mediaId, String mediaType) {
        repository.getVideos(mediaId, mediaType, new Callback<ApiResponse<Video>>() {
            @Override
            public void onResponse(Call<ApiResponse<Video>> call, Response<ApiResponse<Video>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getResults();
                    for (Video v : videos) {
                        if ("YouTube".equals(v.getSite()) && "Trailer".equals(v.getType())) {
                            _trailerKey.setValue(v.getKey());
                            return;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {}
        });
    }

    /**
     * Sprawdza, czy film znajduje się w lokalnej bazie danych Room.
     * 
     * @param movieId Identyfikator filmu.
     */
    public void checkLocalMovieState(int movieId) {
        repository.getMovieById(movieId, movie -> {
            _localMovieState.postValue(movie);
        });
    }

    /**
     * Przełącza status filmu w bibliotece (dodaje/usuwa lub zmienia status obejrzenia).
     * Synchronizuje zmiany z lokalną bazą Room oraz statystykami profilu w Firebase.
     * 
     * @param item Obiekt mediów.
     * @param setAsWatched Czy ustawić status jako "obejrzany".
     */
    public void toggleLibraryStatus(MediaItem item, boolean setAsWatched) {
        MovieEntity currentEntity = _localMovieState.getValue();
        boolean wasAlreadyWatched = currentEntity != null && currentEntity.isWatched();

        if (currentEntity != null && currentEntity.isWatched() == setAsWatched) {
            repository.deleteMovieById(item.getId());
            _localMovieState.postValue(null);
            updateFirebaseStats(item, false, wasAlreadyWatched);
            return;
        }

        boolean isFavorite = currentEntity != null && currentEntity.isFavorite();

        MovieEntity newEntity = new MovieEntity(
                item.getId(),
                item.getTitle(),
                item.getPosterPath(),
                item.getOverview(),
                item.getVoteAverage(),
                item.getMediaType(),
                setAsWatched,
                isFavorite
        );
        repository.insertMovie(newEntity);
        _localMovieState.postValue(newEntity);

        updateFirebaseStats(item, setAsWatched, wasAlreadyWatched);
    }

    /**
     * Aktualizuje liczniki obejrzanych filmów/seriali w profilu użytkownika w Firestore.
     * 
     * @param item Obiekt mediów.
     * @param isMarkingAsWatched Czy użytkownik właśnie zaznaczył element jako obejrzany.
     * @param wasAlreadyWatched Czy element był wcześniej oznaczony jako obejrzany.
     */
    private void updateFirebaseStats(MediaItem item, boolean isMarkingAsWatched, boolean wasAlreadyWatched) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DocumentReference profileRef = FirebaseFirestore.getInstance().collection("profiles").document(uid);

        String fieldToUpdate = "tv".equalsIgnoreCase(item.getMediaType()) ? "stats.tvShowsWatched" : "stats.moviesWatched";

        if (isMarkingAsWatched && !wasAlreadyWatched) {
            profileRef.update(fieldToUpdate, FieldValue.increment(1));
        } else if (!isMarkingAsWatched && wasAlreadyWatched) {
            profileRef.update(fieldToUpdate, FieldValue.increment(-1));
        }
    }
}
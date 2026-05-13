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

public class DetailsViewModel extends AndroidViewModel {

    private final MovieRepository repository;

    private final MutableLiveData<MediaItem> _fullDetails = new MutableLiveData<>();
    public LiveData<MediaItem> fullDetails = _fullDetails;

    private final MutableLiveData<List<Cast>> _cast = new MutableLiveData<>();
    public LiveData<List<Cast>> cast = _cast;

    private final MutableLiveData<String> _trailerKey = new MutableLiveData<>();
    public LiveData<String> trailerKey = _trailerKey;

    private final MutableLiveData<MovieEntity> _localMovieState = new MutableLiveData<>();
    public LiveData<MovieEntity> localMovieState = _localMovieState;

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
    }

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

    public void checkLocalMovieState(int movieId) {
        repository.getMovieById(movieId, movie -> {
            _localMovieState.postValue(movie);
        });
    }

    public void toggleLibraryStatus(MediaItem item, boolean setAsWatched) {
        MovieEntity currentEntity = _localMovieState.getValue();
        boolean wasAlreadyWatched = currentEntity != null && currentEntity.isWatched();

        if (currentEntity != null && currentEntity.isWatched() == setAsWatched) {
            // Cofnięcie statusu / Usunięcie z bazy
            repository.deleteMovieById(item.getId());
            _localMovieState.postValue(null);

            // Zmniejszamy statystykę w Firebase (bo użytkownik cofnął obejrzenie)
            updateFirebaseStats(item, false, wasAlreadyWatched);
            return;
        }

        boolean isFavorite = currentEntity != null && currentEntity.isFavorite();

        // Zapis do bazy lokalnej
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

        // Zwiększamy lub zmniejszamy statystykę w zależności od wybranego statusu
        updateFirebaseStats(item, setAsWatched, wasAlreadyWatched);
    }

    // Dodana metoda do synchronizacji statystyk z Firebase
    private void updateFirebaseStats(MediaItem item, boolean isMarkingAsWatched, boolean wasAlreadyWatched) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DocumentReference profileRef = FirebaseFirestore.getInstance().collection("profiles").document(uid);

        // Rozróżnienie Film vs Serial (TV)
        String fieldToUpdate = "tv".equalsIgnoreCase(item.getMediaType()) ? "stats.tv_shows_watched" : "stats.movies_watched";

        if (isMarkingAsWatched && !wasAlreadyWatched) {
            // Zaznaczono jako obejrzane -> Zwiększamy licznik
            profileRef.update(fieldToUpdate, FieldValue.increment(1));
        } else if (!isMarkingAsWatched && wasAlreadyWatched) {
            // Cofnięto zaznaczenie obejrzanego -> Zmniejszamy licznik
            profileRef.update(fieldToUpdate, FieldValue.increment(-1));
        }
    }
}
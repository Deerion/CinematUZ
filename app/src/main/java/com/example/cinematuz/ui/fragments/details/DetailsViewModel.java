package com.example.cinematuz.ui.fragments.details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cinematuz.data.repositories.MovieRepository;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.Cast;
import com.example.cinematuz.data.models.CreditsResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsViewModel extends ViewModel {

    private final MovieRepository repository = new MovieRepository();

    private final MutableLiveData<MediaItem> _fullDetails = new MutableLiveData<>();
    public LiveData<MediaItem> fullDetails = _fullDetails;

    private final MutableLiveData<List<Cast>> _cast = new MutableLiveData<>();
    public LiveData<List<Cast>> cast = _cast;

    private final MutableLiveData<String> _trailerKey = new MutableLiveData<>();
    public LiveData<String> trailerKey = _trailerKey;

    public void loadData(int id, String type, String lang) {
        repository.getDetails(id, type, lang, new Callback<MediaItem>() {
            @Override
            public void onResponse(Call<MediaItem> call, Response<MediaItem> response) {
                if (response.isSuccessful()) _fullDetails.setValue(response.body());
            }
            @Override
            public void onFailure(Call<MediaItem> call, Throwable t) {}
        });

        repository.getCredits(id, lang, new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) _cast.setValue(response.body().getCast());
            }
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {}
        });
    }

    public void fetchTrailer(int id, String type) {
        repository.getVideos(id, type, new Callback<ApiResponse<Video>>() {
            @Override
            public void onResponse(Call<ApiResponse<Video>> call, Response<ApiResponse<Video>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Video v : response.body().getResults()) {
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
}
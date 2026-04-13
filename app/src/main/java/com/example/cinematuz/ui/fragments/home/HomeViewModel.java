package com.example.cinematuz.ui.fragments.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cinematuz.data.repositories.MovieRepository; // Używamy repozytorium
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private final MovieRepository repository = new MovieRepository();

    private final MutableLiveData<List<MediaItem>> _trendingList = new MutableLiveData<>();
    public LiveData<List<MediaItem>> trendingList = _trendingList;

    private final MutableLiveData<MediaItem> _heroItem = new MutableLiveData<>();
    public LiveData<MediaItem> heroItem = _heroItem;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private List<MediaItem> allItemsBuffer = new ArrayList<>();

    public void fetchTrending(String lang) {
        if (allItemsBuffer.isEmpty()) _isLoading.setValue(true);

        repository.getTrending(lang, new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allItemsBuffer = response.body().getResults();
                    applyFilter("all");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                _isLoading.setValue(false);
            }
        });
    }

    public void applyFilter(String type) {
        if (allItemsBuffer.isEmpty()) return;
        List<MediaItem> filtered = new ArrayList<>();
        MediaItem firstMatch = null;

        for (MediaItem item : allItemsBuffer) {
            if (type.equals("all") || type.equals(item.getMediaType())) {
                if (firstMatch == null) firstMatch = item;
                else filtered.add(item);
            }
        }
        _heroItem.setValue(firstMatch);
        _trendingList.setValue(filtered);
    }
}
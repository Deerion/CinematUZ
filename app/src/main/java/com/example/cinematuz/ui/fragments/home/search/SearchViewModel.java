package com.example.cinematuz.ui.fragments.home.search;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.repositories.MovieRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.text.TextUtils;
import com.example.cinematuz.data.models.FilterCriteria;

public class SearchViewModel extends ViewModel {

    private final MovieRepository repository = new MovieRepository();

    private final MutableLiveData<List<MediaItem>> _searchResults = new MutableLiveData<>();
    public LiveData<List<MediaItem>> searchResults = _searchResults;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public void onSearchTextChanged(String query, String lang) {
        if (searchRunnable != null) {
            handler.removeCallbacks(searchRunnable);
        }

        if (query == null || query.trim().isEmpty()) {
            _searchResults.setValue(null);
            _isLoading.setValue(false);
            return;
        }

        // Opóźnienie 500ms (Debounce)
        searchRunnable = () -> performSearch(query.trim(), lang);
        handler.postDelayed(searchRunnable, 500);
    }

    private void performSearch(String query, String lang) {
        _isLoading.setValue(true);
        repository.searchMulti(query, lang, 1, new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _searchResults.setValue(response.body().getResults());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                _isLoading.setValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
    }

    public void applyAdvancedFilters(FilterCriteria criteria, String lang) {
        _isLoading.setValue(true);

        String dateFrom = criteria.yearFrom + "-01-01";
        String dateTo = criteria.yearTo + "-12-31";

        // Konwersja listy ID gatunków na string oddzielony przecinkami np. "28,878"
        String genresString = TextUtils.join(",", criteria.genreIds);

        repository.discoverContent(
                criteria.contentType,
                lang,
                criteria.sortBy,
                dateFrom,
                dateTo,
                criteria.minRating,
                genresString,
                new Callback<ApiResponse<MediaItem>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                        _isLoading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            _searchResults.setValue(response.body().getResults());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                        _isLoading.setValue(false);
                    }
                }
        );
    }
}
package com.example.cinematuz.ui.fragments.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.repositories.MovieRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel dla ekranu głównego (HomeFragment).
 * Zarządza pobieraniem trendujących treści oraz ich filtrowaniem.
 */
public class HomeViewModel extends AndroidViewModel {

    private final MovieRepository repository;

    private final MutableLiveData<List<MediaItem>> _trendingList = new MutableLiveData<>();
    /** Lista trendujących filmów i seriali (bez elementu wyróżnionego). */
    public LiveData<List<MediaItem>> trendingList = _trendingList;

    private final MutableLiveData<MediaItem> _heroItem = new MutableLiveData<>();
    /** Wyróżniony element mediów wyświetlany w sekcji Hero. */
    public LiveData<MediaItem> heroItem = _heroItem;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    /** Status ładowania danych. */
    public LiveData<Boolean> isLoading = _isLoading;

    private List<MediaItem> allItems = new ArrayList<>();
    private String currentFilter = "all";

    /**
     * Inicjalizuje ViewModel i repozytorium.
     * 
     * @param application Kontekst aplikacji.
     */
    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
    }

    /**
     * Pobiera listę trendujących treści z repozytorium.
     * 
     * @param lang Kod języka dla wyników wyszukiwania.
     */
    public void fetchTrending(String lang) {
        _isLoading.setValue(true);

        repository.getTrending(lang, new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(Call<ApiResponse<MediaItem>> call, Response<ApiResponse<MediaItem>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    allItems = response.body().getResults();
                    applyFilter(currentFilter);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MediaItem>> call, Throwable t) {
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Filtruje pobrane dane według typu mediów i aktualizuje widoczne listy.
     * Pierwszy pasujący element trafia do sekcji Hero, reszta do listy trendów.
     * 
     * @param filterType Typ mediów do wyświetlenia ("all", "movie", "tv").
     */
    public void applyFilter(String filterType) {
        currentFilter = filterType;
        if (allItems.isEmpty()) return;

        List<MediaItem> filteredList = new ArrayList<>();
        MediaItem newHero = null;

        for (MediaItem item : allItems) {
            boolean matches = "all".equals(filterType) || item.getMediaType().equals(filterType);

            if (matches) {
                if (newHero == null) {
                    newHero = item;
                } else {
                    filteredList.add(item);
                }
            }
        }

        _heroItem.setValue(newHero);
        _trendingList.setValue(filteredList);
    }
}
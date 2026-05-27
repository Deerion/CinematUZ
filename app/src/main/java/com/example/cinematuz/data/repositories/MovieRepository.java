package com.example.cinematuz.data.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cinematuz.data.api.RetrofitClient;
import com.example.cinematuz.data.api.TmdbApi;
import com.example.cinematuz.data.local.AppDatabase;
import com.example.cinematuz.data.local.MovieDao;
import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.CreditsResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Callback;

/**
 * Repozytorium zarządzające danymi filmów i seriali.
 * Łączy operacje sieciowe (Retrofit/TMDB) z lokalną bazą danych (Room).
 */
public class MovieRepository {

    private final TmdbApi api;
    private final MovieDao movieDao;
    private final ExecutorService executorService;

    /**
     * Konstruktor repozytorium inicjalizujący API oraz lokalną bazę danych.
     * 
     * @param application Kontekst aplikacji.
     */
    public MovieRepository(Application application) {
        this.api = RetrofitClient.getClient().create(TmdbApi.class);
        AppDatabase db = AppDatabase.getInstance(application);
        this.movieDao = db.movieDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * Konstruktor przeznaczony do testów jednostkowych, pozwalający na wstrzyknięcie mocków.
     * 
     * @param api Mock interfejsu API.
     * @param movieDao Mock obiektu DAO.
     */
    public MovieRepository(TmdbApi api, MovieDao movieDao) {
        this.api = api;
        this.movieDao = movieDao;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    // --- ORYGINALNE METODY SIECIOWE (TMDB) ---

    /**
     * Pobiera trendy z API.
     * 
     * @param lang Kod języka.
     * @param callback Callback obsługujący odpowiedź.
     */
    public void getTrending(String lang, Callback<ApiResponse<MediaItem>> callback) {
        api.getTrending(lang, 1).enqueue(callback);
    }

    /**
     * Wyszukuje treści w API.
     * 
     * @param query Fraza wyszukiwania.
     * @param lang Kod języka.
     * @param page Numer strony.
     * @param callback Callback obsługujący odpowiedź.
     */
    public void searchMulti(String query, String lang, int page, Callback<ApiResponse<MediaItem>> callback) {
        api.searchMulti(query, lang, page).enqueue(callback);
    }

    /**
     * Pobiera szczegóły filmu lub serialu.
     * 
     * @param id Identyfikator elementu.
     * @param type Typ ("tv" lub "movie").
     * @param lang Kod języka.
     * @param callback Callback obsługujący odpowiedź.
     */
    public void getDetails(int id, String type, String lang, Callback<MediaItem> callback) {
        if ("tv".equals(type)) {
            api.getTvDetails(id, lang).enqueue(callback);
        } else {
            api.getMovieDetails(id, lang).enqueue(callback);
        }
    }

    /**
     * Pobiera informacje o obsadzie.
     * 
     * @param id Identyfikator elementu.
     * @param type Typ ("tv" lub "movie").
     * @param lang Kod języka.
     * @param callback Callback obsługujący odpowiedź.
     */
    public void getCredits(int id, String type, String lang, Callback<CreditsResponse> callback) {
        if ("tv".equals(type)) {
            api.getTvCredits(id, lang).enqueue(callback);
        } else {
            api.getMovieCredits(id, lang).enqueue(callback);
        }
    }

    /**
     * Pobiera materiały wideo (np. trailery).
     * 
     * @param id Identyfikator elementu.
     * @param type Typ ("tv" lub "movie").
     * @param callback Callback obsługujący odpowiedź.
     */
    public void getVideos(int id, String type, Callback<ApiResponse<Video>> callback) {
        if ("tv".equals(type)) {
            api.getTvVideos(id, "en-US").enqueue(callback);
        } else {
            api.getMovieVideos(id, "en-US").enqueue(callback);
        }
    }

    /**
     * Odkrywa treści na podstawie filtrów.
     * 
     * @param type Typ ("tv" lub "movie").
     * @param lang Kod języka.
     * @param sortBy Sposób sortowania.
     * @param dateFrom Data od.
     * @param dateTo Data do.
     * @param minRating Minimalna ocena.
     * @param genres Gatunki.
     * @param callback Callback obsługujący odpowiedź.
     */
    public void discoverContent(String type, String lang, String sortBy, String dateFrom, String dateTo, float minRating, String genres, Callback<ApiResponse<MediaItem>> callback) {
        if ("tv".equals(type)) {
            api.discoverTv(lang, sortBy, dateFrom, dateTo, minRating, genres, 1).enqueue(callback);
        } else {
            api.discoverMovies(lang, sortBy, dateFrom, dateTo, minRating, genres, 1).enqueue(callback);
        }
    }

    // --- NOWE METODY LOKALNEJ BAZY DANYCH (ROOM) ---

    /**
     * Wstawia film do lokalnej bazy danych (operacja asynchroniczna).
     * 
     * @param movie Encja filmu.
     */
    public void insertMovie(MovieEntity movie) {
        executorService.execute(() -> movieDao.insertMovie(movie));
    }

    /**
     * Usuwa film z lokalnej bazy danych po ID (operacja asynchroniczna).
     * 
     * @param movieId Identyfikator filmu.
     */
    public void deleteMovieById(int movieId) {
        executorService.execute(() -> movieDao.deleteMovieById(movieId));
    }

    /**
     * Pobiera filmy z lokalnej bazy danych na podstawie statusu obejrzenia.
     * 
     * @param isWatched Status obejrzenia.
     * @return LiveData z listą filmów.
     */
    public LiveData<List<MovieEntity>> getMoviesByWatchStatus(boolean isWatched) {
        return movieDao.getMoviesByWatchStatus(isWatched);
    }

    /**
     * Pobiera film z lokalnej bazy danych po ID.
     * 
     * @param id Identyfikator filmu.
     * @param listener Listener wyniku.
     */
    public void getMovieById(int id, OnMovieCheckListener listener) {
        executorService.execute(() -> {
            MovieEntity movie = movieDao.getMovieById(id);
            listener.onResult(movie);
        });
    }

    /**
     * Interfejs callbacku dla operacji sprawdzania filmu w bazie.
     */
    public interface OnMovieCheckListener {
        /**
         * Wywoływane po pobraniu wyniku z bazy.
         * 
         * @param movie Pobrana encja filmu lub null.
         */
        void onResult(MovieEntity movie);
    }
}
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

public class MovieRepository {

    private final TmdbApi api;
    private final MovieDao movieDao;
    private final ExecutorService executorService;

    // NOWY KONSTRUKTOR WYMAGAJĄCY APPLICATION
    public MovieRepository(Application application) {
        this.api = RetrofitClient.getClient().create(TmdbApi.class);

        AppDatabase db = AppDatabase.getInstance(application);
        this.movieDao = db.movieDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    // --- ORYGINALNE METODY SIECIOWE (TMDB) ---

    public void getTrending(String lang, Callback<ApiResponse<MediaItem>> callback) {
        api.getTrending(lang, 1).enqueue(callback);
    }

    public void searchMulti(String query, String lang, int page, Callback<ApiResponse<MediaItem>> callback) {
        api.searchMulti(query, lang, page).enqueue(callback);
    }

    public void getDetails(int id, String type, String lang, Callback<MediaItem> callback) {
        if ("tv".equals(type)) {
            api.getTvDetails(id, lang).enqueue(callback);
        } else {
            api.getMovieDetails(id, lang).enqueue(callback);
        }
    }

    public void getCredits(int id, String lang, Callback<CreditsResponse> callback) {
        api.getMovieCredits(id, lang).enqueue(callback);
    }

    public void getVideos(int id, String type, Callback<ApiResponse<Video>> callback) {
        if ("tv".equals(type)) {
            api.getTvVideos(id, "en-US").enqueue(callback);
        } else {
            api.getMovieVideos(id, "en-US").enqueue(callback);
        }
    }

    public void discoverContent(String type, String lang, String sortBy, String dateFrom, String dateTo, float minRating, String genres, Callback<ApiResponse<MediaItem>> callback) {
        if ("tv".equals(type)) {
            api.discoverTv(lang, sortBy, dateFrom, dateTo, minRating, genres, 1).enqueue(callback);
        } else {
            api.discoverMovies(lang, sortBy, dateFrom, dateTo, minRating, genres, 1).enqueue(callback);
        }
    }

    // --- NOWE METODY LOKALNEJ BAZY DANYCH (ROOM) ---

    public void insertMovie(MovieEntity movie) {
        executorService.execute(() -> movieDao.insertMovie(movie));
    }

    public void deleteMovieById(int movieId) {
        executorService.execute(() -> movieDao.deleteMovieById(movieId));
    }

    public LiveData<List<MovieEntity>> getMoviesByWatchStatus(boolean isWatched) {
        return movieDao.getMoviesByWatchStatus(isWatched);
    }

    public void getMovieById(int id, OnMovieCheckListener listener) {
        executorService.execute(() -> {
            MovieEntity movie = movieDao.getMovieById(id);
            listener.onResult(movie);
        });
    }

    public interface OnMovieCheckListener {
        void onResult(MovieEntity movie);
    }
}
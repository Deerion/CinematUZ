package com.example.cinematuz.data.repositories;

import com.example.cinematuz.data.api.RetrofitClient;
import com.example.cinematuz.data.api.TmdbApi;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.CreditsResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;

import retrofit2.Callback;

public class MovieRepository {

    private final TmdbApi api;

    public MovieRepository() {
        this.api = RetrofitClient.getClient().create(TmdbApi.class);
    }

    public void getTrending(String lang, Callback<ApiResponse<MediaItem>> callback) {
        api.getTrending(lang, 1).enqueue(callback);
    }

    // --- NOWA METODA WYSZUKIWANIA ---
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
}
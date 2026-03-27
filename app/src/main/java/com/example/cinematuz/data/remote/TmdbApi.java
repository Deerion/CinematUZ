package com.example.cinematuz.data.remote;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;
import com.example.cinematuz.data.models.CreditsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TmdbApi {

    // --- 1. HOME (Zalecane /trending/all/day dla filmów i seriali) ---
    @GET("trending/all/day")
    Call<ApiResponse<MediaItem>> getTrending(
            @Query("language") String language,
            @Query("page") int page
    );

    // Opcjonalnie: Tylko popularne filmy (gdybyś jednak wolała oddzielić)
    @GET("movie/popular")
    Call<ApiResponse<MediaItem>> getPopularMovies(
            @Query("language") String language,
            @Query("page") int page
    );

    // --- 2. WYSZUKIWARKA (Szuka filmów, seriali i aktorów naraz) ---
    @GET("search/multi")
    Call<ApiResponse<MediaItem>> searchMulti(
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    // --- 3. DETALE ---
    // Detale filmu
    @GET("movie/{movie_id}")
    Call<MediaItem> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    // Detale serialu
    @GET("tv/{tv_id}")
    Call<MediaItem> getTvDetails(
            @Path("tv_id") int tvId,
            @Query("language") String language
    );

    // --- 4. OBSADA (Credits) ---
    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    // --- 5. TRAILERY (Videos z YouTube) ---
    @GET("movie/{movie_id}/videos")
    Call<ApiResponse<Video>> getMovieVideos(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    // TRAILERY DLA SERIALI (Videos z YouTube)
    @GET("tv/{tv_id}/videos")
    Call<ApiResponse<Video>> getTvVideos(
            @Path("tv_id") int tvId,
            @Query("language") String language
    );
}
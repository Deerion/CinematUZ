package com.example.cinematuz.data.api;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;
import com.example.cinematuz.data.models.CreditsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interfejs Retrofita do komunikacji z API The Movie Database (TMDB).
 * Definiuje punkty końcowe dla pobierania trendów, wyszukiwania, szczegółów filmów i seriali.
 */
public interface TmdbApi {

    /**
     * Pobiera aktualnie popularne filmy i seriale.
     * 
     * @param language Kod języka (np. "pl-PL").
     * @param page Numer strony wyników.
     * @return Obiekt Call z odpowiedzią zawierającą listę trendujących elementów.
     */
    @GET("trending/all/day")
    Call<ApiResponse<MediaItem>> getTrending(
            @Query("language") String language,
            @Query("page") int page
    );

    /**
     * Pobiera listę popularnych filmów.
     * 
     * @param language Kod języka.
     * @param page Numer strony.
     * @return Obiekt Call z listą popularnych filmów.
     */
    @GET("movie/popular")
    Call<ApiResponse<MediaItem>> getPopularMovies(
            @Query("language") String language,
            @Query("page") int page
    );

    /**
     * Wyszukuje filmy, seriale i osoby na podstawie podanego zapytania.
     * 
     * @param query Fraza wyszukiwania.
     * @param language Kod języka.
     * @param page Numer strony.
     * @return Obiekt Call z wynikami wyszukiwania.
     */
    @GET("search/multi")
    Call<ApiResponse<MediaItem>> searchMulti(
            @Query("query") String query,
            @Query("language") String language,
            @Query("page") int page
    );

    /**
     * Pobiera listę płac (obsadę i ekipę) dla serialu TV.
     * 
     * @param tvId Identyfikator serialu.
     * @param language Kod języka.
     * @return Obiekt Call z danymi o obsadzie.
     */
    @GET("tv/{tv_id}/credits")
    Call<CreditsResponse> getTvCredits(@Path("tv_id") int tvId, @Query("language") String language);

    /**
     * Pobiera szczegółowe informacje o konkretnym filmie.
     * 
     * @param movieId Identyfikator filmu.
     * @param language Kod języka.
     * @return Obiekt Call ze szczegółami filmu.
     */
    @GET("movie/{movie_id}")
    Call<MediaItem> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    /**
     * Pobiera szczegółowe informacje o konkretnym serialu.
     * 
     * @param tvId Identyfikator serialu.
     * @param language Kod języka.
     * @return Obiekt Call ze szczegółami serialu.
     */
    @GET("tv/{tv_id}")
    Call<MediaItem> getTvDetails(
            @Path("tv_id") int tvId,
            @Query("language") String language
    );

    /**
     * Pobiera listę płac (obsadę i ekipę) dla filmu.
     * 
     * @param movieId Identyfikator filmu.
     * @param language Kod języka.
     * @return Obiekt Call z danymi o obsadzie.
     */
    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getMovieCredits(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    /**
     * Pobiera materiały wideo (np. trailery) powiązane z filmem.
     * 
     * @param movieId Identyfikator filmu.
     * @param language Kod języka.
     * @return Obiekt Call z listą wideo.
     */
    @GET("movie/{movie_id}/videos")
    Call<ApiResponse<Video>> getMovieVideos(
            @Path("movie_id") int movieId,
            @Query("language") String language
    );

    /**
     * Pobiera materiały wideo (np. trailery) powiązane z serialem.
     * 
     * @param tvId Identyfikator serialu.
     * @param language Kod języka.
     * @return Obiekt Call z listą wideo.
     */
    @GET("tv/{tv_id}/videos")
    Call<ApiResponse<Video>> getTvVideos(
            @Path("tv_id") int tvId,
            @Query("language") String language
    );

    /**
     * Zaawansowane wyszukiwanie filmów z użyciem filtrów.
     * 
     * @param language Kod języka.
     * @param sortBy Sposób sortowania wyników.
     * @param dateFrom Data początkowa (od kiedy).
     * @param dateTo Data końcowa (do kiedy).
     * @param minRating Minimalna średnia ocena.
     * @param genreIds Lista identyfikatorów gatunków (oddzielona przecinkami).
     * @param page Numer strony.
     * @return Obiekt Call z przefiltrowaną listą filmów.
     */
    @GET("discover/movie")
    Call<ApiResponse<MediaItem>> discoverMovies(
            @Query("language") String language,
            @Query("sort_by") String sortBy,
            @Query("primary_release_date.gte") String dateFrom,
            @Query("primary_release_date.lte") String dateTo,
            @Query("vote_average.gte") float minRating,
            @Query("with_genres") String genreIds,
            @Query("page") int page
    );

    /**
     * Zaawansowane wyszukiwanie seriali z użyciem filtrów.
     * 
     * @param language Kod języka.
     * @param sortBy Sposób sortowania wyników.
     * @param dateFrom Data początkowa.
     * @param dateTo Data końcowa.
     * @param minRating Minimalna średnia ocena.
     * @param genreIds Lista identyfikatorów gatunków.
     * @param page Numer strony.
     * @return Obiekt Call z przefiltrowaną listą seriali.
     */
    @GET("discover/tv")
    Call<ApiResponse<MediaItem>> discoverTv(
            @Query("language") String language,
            @Query("sort_by") String sortBy,
            @Query("first_air_date.gte") String dateFrom,
            @Query("first_air_date.lte") String dateTo,
            @Query("vote_average.gte") float minRating,
            @Query("with_genres") String genreIds,
            @Query("page") int page
    );
}
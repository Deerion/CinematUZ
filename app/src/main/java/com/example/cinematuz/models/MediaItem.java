package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;

public class MediaItem {

    @SerializedName("id")
    private int id;

    // Pobierze "title" (dla filmów) LUB "name" (dla seriali)
    @SerializedName(value = "title", alternate = {"name"})
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    // Pobierze "release_date" (dla filmów) LUB "first_air_date" (dla seriali)
    @SerializedName(value = "release_date", alternate = {"first_air_date"})
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    // Warto dodać to pole, aby rozróżnić typ na liście (TMDB zwraca je w wynikach wyszukiwania i listach "Trending")
    @SerializedName("media_type")
    private String mediaType; // Przyjmuje wartości np. "movie" lub "tv"

    // Gettery
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getReleaseDate() { return releaseDate; }
    public double getVoteAverage() { return voteAverage; }
    public String getMediaType() { return mediaType; }
}
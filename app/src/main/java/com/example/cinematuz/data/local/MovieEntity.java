package com.example.cinematuz.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Encja reprezentująca film w lokalnej bazie danych Room.
 * Przechowuje podstawowe informacje o filmie oraz jego status w bibliotece użytkownika.
 */
@Entity(tableName = "movies_table")
public class MovieEntity {

    /**
     * Unikalny identyfikator filmu (zgodny z TMDB).
     */
    @PrimaryKey
    private int id;

    private String title;
    private String posterPath;
    private String overview;
    private double voteAverage;
    private String mediaType;

    /**
     * Flaga określająca, czy film został już obejrzany przez użytkownika.
     */
    private boolean isWatched;

    /**
     * Flaga określająca, czy film został dodany do ulubionych.
     */
    private boolean isFavorite;

    /**
     * Konstruktor tworzący nową encję filmu.
     * 
     * @param id Unikalny identyfikator.
     * @param title Tytuł filmu.
     * @param posterPath Ścieżka do plakatu.
     * @param overview Opis filmu.
     * @param voteAverage Średnia ocen.
     * @param mediaType Typ mediów (np. "movie" lub "tv").
     * @param isWatched Czy obejrzany.
     * @param isFavorite Czy ulubiony.
     */
    public MovieEntity(int id, String title, String posterPath, String overview, double voteAverage, String mediaType, boolean isWatched, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.mediaType = mediaType;
        this.isWatched = isWatched;
        this.isFavorite = isFavorite;
    }

    // Gettery i Settery

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public boolean isWatched() { return isWatched; }
    public void setWatched(boolean watched) { this.isWatched = watched; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite; }
}
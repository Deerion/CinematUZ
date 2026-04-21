package com.example.cinematuz.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movies_table")
public class MovieEntity {

    // ID z bazy TMDB
    @PrimaryKey
    private int id;

    private String title;
    private String posterPath;
    private String overview;
    private double voteAverage;
    private String mediaType; // "movie" lub "tv"

    // false = "Do obejrzenia", true = "Obejrzane"
    private boolean isWatched;

    public MovieEntity(int id, String title, String posterPath, String overview, double voteAverage, String mediaType, boolean isWatched) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.mediaType = mediaType;
        this.isWatched = isWatched;
    }

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
    public void setWatched(boolean watched) { isWatched = watched; }
}
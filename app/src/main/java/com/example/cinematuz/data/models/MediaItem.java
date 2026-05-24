package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class MediaItem implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName(value = "title", alternate = {"name"})
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName(value = "release_date", alternate = {"first_air_date"})
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("runtime")
    private Integer runtime;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    @SerializedName("genres")
    private List<Genre> genres;

    public static class Genre implements Serializable {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        public int getId() { return id; }
        public String getName() { return name; }
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getReleaseDate() { return releaseDate; }
    public double getVoteAverage() { return voteAverage; }
    public String getMediaType() { return mediaType; }
    public Integer getRuntime() { return runtime; }
    public List<Integer> getGenreIds() { return genreIds; }
    public List<Genre> getGenres() { return genres; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setOverview(String overview) { this.overview = overview; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    // DODANE SETTERY DO OBSŁUGI BŁĘDÓW KOMPILACJI
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setGenres(List<Genre> genres) { this.genres = genres; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }
}
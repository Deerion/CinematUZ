package com.example.cinematuz.data.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class User {
    private String username;
    private String email;
    private String avatar_url;
    private UserStats stats = new UserStats();

    public User() {}

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.avatar_url = "";
        this.stats = new UserStats();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public UserStats getStats() { return stats; }
    public void setStats(UserStats stats) { this.stats = stats; }

    public static class UserStats {
        @PropertyName("movies_watched")
        private int moviesWatched;

        @PropertyName("tv_shows_watched")
        private int tvShowsWatched;

        public UserStats() {
            this.moviesWatched = 0;
            this.tvShowsWatched = 0;
        }

        @PropertyName("movies_watched")
        public int getMoviesWatched() { return moviesWatched; }
        @PropertyName("movies_watched")
        public void setMoviesWatched(int moviesWatched) { this.moviesWatched = moviesWatched; }

        @PropertyName("tv_shows_watched")
        public int getTvShowsWatched() { return tvShowsWatched; }
        @PropertyName("tv_shows_watched")
        public void setTvShowsWatched(int tvShowsWatched) { this.tvShowsWatched = tvShowsWatched; }
    }
}
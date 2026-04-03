package com.example.cinematuz.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

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
        private int moviesWatched;
        private int reviewsCount;
        private int points;

        public UserStats() {
            this.moviesWatched = 0;
            this.reviewsCount = 0;
            this.points = 0;
        }

        public int getMoviesWatched() { return moviesWatched; }
        public void setMoviesWatched(int moviesWatched) { this.moviesWatched = moviesWatched; }

        public int getReviewsCount() { return reviewsCount; }
        public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
    }
}
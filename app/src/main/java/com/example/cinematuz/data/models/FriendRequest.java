package com.example.cinematuz.data.models;

public class FriendRequest {
    private String uid;
    private String username;
    private String avatarUrl;
    private String type; // "friend" lub "group"

    public FriendRequest() {} // Wymagany przez Firebase

    public FriendRequest(String uid, String username, String avatarUrl, String type) {
        this.uid = uid;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.type = type;
    }

    // --- GETTERY ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getType() { return type; }

    // --- SETTERY (Brakowało ich!) ---
    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setType(String type) { this.type = type; }
}
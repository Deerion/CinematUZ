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

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getType() { return type; }
}
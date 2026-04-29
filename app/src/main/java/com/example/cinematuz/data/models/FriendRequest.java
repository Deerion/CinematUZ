// Lokalizacja: java/com/example/cinematuz/data/models/FriendRequest.java
package com.example.cinematuz.data.models;

public class FriendRequest {
    private String uid;
    private String username;
    private String avatarUrl;

    public FriendRequest(String uid, String username, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
}
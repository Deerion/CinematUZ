// Lokalizacja: java/com/example/cinematuz/data/models/Friend.java
package com.example.cinematuz.data.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Friend {
    private String id;
    private String name;
    private String avatarUrl;
    private boolean isOnline;
    private String status = "accepted"; // Domyślnie zaakceptowany, może być "pending"

    public Friend() {}

    public Friend(String id, String name, String avatarUrl, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.isOnline = isOnline;
        this.status = "accepted";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
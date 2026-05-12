package com.example.cinematuz.data.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class Group {
    private String id; // ID dokumentu z Firestore
    private String name;
    private String ownerId;
    private List<String> members;

    @ServerTimestamp
    private Date createdAt; // Firebase automatycznie wstawi tu datę serwera

    // Pusty konstruktor wymagany przez Firebase
    public Group() {}

    public Group(String name, String ownerId, List<String> members) {
        this.name = name;
        this.ownerId = ownerId;
        this.members = members;
    }

    // Gettery i Settery
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
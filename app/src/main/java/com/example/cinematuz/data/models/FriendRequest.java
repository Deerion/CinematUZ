package com.example.cinematuz.data.models;

/**
 * Model danych reprezentujący zaproszenie do znajomych lub zaproszenie do grupy.
 */
public class FriendRequest {
    private String uid;
    private String username;
    private String avatarUrl;
    private String type; // "friend" lub "group"

    /**
     * Pusty konstruktor wymagany przez Firebase Firestore.
     */
    public FriendRequest() {}

    /**
     * Konstruktor tworzący obiekt zaproszenia.
     * 
     * @param uid Unikalny identyfikator wysyłającego.
     * @param username Nazwa użytkownika wysyłającego.
     * @param avatarUrl Adres URL do obrazka profilowego wysyłającego.
     * @param type Typ zaproszenia ("friend" lub "group").
     */
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

    // --- SETTERY ---
    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setType(String type) { this.type = type; }
}
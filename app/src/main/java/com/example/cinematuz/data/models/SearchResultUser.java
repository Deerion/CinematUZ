package com.example.cinematuz.data.models;

/**
 * Model danych reprezentujący użytkownika znalezionego w wynikach wyszukiwania.
 * Zawiera podstawowe informacje profilowe widoczne dla innych użytkowników.
 */
public class SearchResultUser {
    private String uid;
    private String username;
    private String avatarUrl;

    /**
     * Pusty konstruktor wymagany przez Firebase Firestore.
     */
    public SearchResultUser() {}

    /**
     * Konstruktor tworzący obiekt wyniku wyszukiwania użytkownika.
     * 
     * @param uid Unikalny identyfikator użytkownika.
     * @param username Nazwa użytkownika.
     * @param avatarUrl Adres URL do obrazka profilowego.
     */
    public SearchResultUser(String uid, String username, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    /**
     * @return Unikalny identyfikator użytkownika.
     */
    public String getUid() { return uid; }

    /**
     * @return Nazwa użytkownika.
     */
    public String getUsername() { return username; }

    /**
     * @return Adres URL do obrazka profilowego.
     */
    public String getAvatarUrl() { return avatarUrl; }
}
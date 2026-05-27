package com.example.cinematuz.data.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

/**
 * Model danych reprezentujący grupę użytkowników w aplikacji.
 * Wykorzystywany do wspólnego wybierania filmów.
 */
public class Group {
    private String id; // ID dokumentu z Firestore
    private String name;
    private String ownerId;
    private List<String> members;
    private String winnerId;
    private String winnerReason;

    @ServerTimestamp
    private Date createdAt; // Firebase automatycznie wstawi tu datę serwera

    /**
     * Pusty konstruktor wymagany przez Firebase Firestore.
     */
    public Group() {
    }

    /**
     * Konstruktor tworzący nową grupę.
     * 
     * @param name Nazwa grupy.
     * @param ownerId Unikalny identyfikator właściciela grupy.
     * @param members Lista identyfikatorów członków grupy.
     */
    public Group(String name, String ownerId, List<String> members) {
        this.name = name;
        this.ownerId = ownerId;
        this.members = members;
    }

    // Gettery i Settery

    /**
     * @return Unikalny identyfikator dokumentu grupy.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Nazwa grupy.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Unikalny identyfikator właściciela.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return Lista identyfikatorów członków.
     */
    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    /**
     * @return Identyfikator wybranego filmu ("zwycięzcy").
     */
    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    /**
     * @return Powód wyboru danego filmu.
     */
    public String getWinnerReason() {
        return winnerReason;
    }

    public void setWinnerReason(String winnerReason) {
        this.winnerReason = winnerReason;
    }

    /**
     * @return Data utworzenia grupy.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
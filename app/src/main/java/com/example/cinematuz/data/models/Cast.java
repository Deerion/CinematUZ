package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model danych reprezentujący członka obsady filmu lub serialu.
 */
public class Cast {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("character")
    private String character;

    @SerializedName("profile_path")
    private String profilePath;

    // Gettery
    /**
     * @return Unikalny identyfikator aktora.
     */
    public int getId() { return id; }

    /**
     * @return Imię i nazwisko aktora.
     */
    public String getName() { return name; }

    /**
     * @return Nazwa postaci granej przez aktora.
     */
    public String getCharacter() { return character; }

    /**
     * @return Ścieżka do zdjęcia profilowego aktora.
     */
    public String getProfilePath() { return profilePath; }
}
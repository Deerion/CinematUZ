package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model danych reprezentujący materiał wideo (np. trailer, teaser) powiązany z filmem lub serialem.
 * Dane pochodzą z API TMDB.
 */
public class Video {

    @SerializedName("id")
    private String id;

    @SerializedName("key")
    private String key; // To jest ID filmu na YouTube (np. "dQw4w9WgXcQ")

    @SerializedName("name")
    private String name;

    @SerializedName("site")
    private String site; // Np. "YouTube"

    @SerializedName("type")
    private String type; // Np. "Trailer", "Teaser", "Featurette"

    // Gettery
    /**
     * @return Unikalny identyfikator wideo w systemie TMDB.
     */
    public String getId() { return id; }

    /**
     * @return Klucz wideo (np. identyfikator filmu w serwisie YouTube).
     */
    public String getKey() { return key; }

    /**
     * @return Nazwa/tytuł materiału wideo.
     */
    public String getName() { return name; }

    /**
     * @return Nazwa serwisu hostującego wideo (np. "YouTube").
     */
    public String getSite() { return site; }

    /**
     * @return Typ materiału wideo (np. "Trailer", "Teaser").
     */
    public String getType() { return type; }
}
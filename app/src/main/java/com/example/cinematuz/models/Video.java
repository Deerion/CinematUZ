package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;

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
    public String getId() { return id; }
    public String getKey() { return key; }
    public String getName() { return name; }
    public String getSite() { return site; }
    public String getType() { return type; }
}
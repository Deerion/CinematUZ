package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model odpowiedzi z API TMDB zawierający listę płac (obsadę) filmu lub serialu.
 */
public class CreditsResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("cast")
    private List<Cast> cast;

    /**
     * @return Identyfikator elementu mediów.
     */
    public int getId() { return id; }

    /**
     * @return Lista członków obsady.
     */
    public List<Cast> getCast() { return cast; }
}
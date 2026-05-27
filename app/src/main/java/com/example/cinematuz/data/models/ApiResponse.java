package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Generyczna klasa odpowiedzi z API TMDB.
 * Przechowuje informacje o paginacji oraz listę wyników określonego typu.
 * 
 * @param <T> Typ danych zawartych w liście wyników (np. MediaItem, Video).
 */
public class ApiResponse<T> {

    @SerializedName("page")
    private int page;

    @SerializedName("results")
    private List<T> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    // Gettery
    /**
     * @return Numer aktualnej strony.
     */
    public int getPage() { return page; }

    /**
     * @return Lista wyników zwróconych przez API.
     */
    public List<T> getResults() { return results; }

    /**
     * @return Całkowita liczba dostępnych stron.
     */
    public int getTotalPages() { return totalPages; }

    /**
     * @return Całkowita liczba wyników dla zapytania.
     */
    public int getTotalResults() { return totalResults; }
}
package com.example.cinematuz.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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
    public int getPage() { return page; }
    public List<T> getResults() { return results; }
    public int getTotalPages() { return totalPages; }
    public int getTotalResults() { return totalResults; }
}
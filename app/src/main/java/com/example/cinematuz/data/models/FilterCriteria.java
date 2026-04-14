package com.example.cinematuz.data.models;

import java.io.Serializable;
import java.util.List;

public class FilterCriteria implements Serializable {
    public String sortBy;
    public String contentType;
    public List<Integer> genreIds;
    public int yearFrom;
    public int yearTo;
    public float minRating;
}
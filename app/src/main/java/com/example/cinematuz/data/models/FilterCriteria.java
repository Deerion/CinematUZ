package com.example.cinematuz.data.models;

import java.io.Serializable;
import java.util.List;

/**
 * Model danych przechowujący kryteria filtrowania dla wyszukiwania filmów i seriali.
 * Wykorzystywany do przekazywania parametrów filtrowania między fragmentami.
 */
public class FilterCriteria implements Serializable {
    /** Metoda sortowania (np. popularity.desc). */
    public String sortBy;
    /** Typ treści ("movie" lub "tv"). */
    public String contentType;
    /** Lista identyfikatorów gatunków. */
    public List<Integer> genreIds;
    /** Rok początkowy zakresu wyszukiwania. */
    public int yearFrom;
    /** Rok końcowy zakresu wyszukiwania. */
    public int yearTo;
    /** Minimalna średnia ocena. */
    public float minRating;
}
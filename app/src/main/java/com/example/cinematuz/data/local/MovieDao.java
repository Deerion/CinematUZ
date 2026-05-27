package com.example.cinematuz.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Interfejs DAO (Data Access Object) do obsługi operacji na tabeli filmów w bazie danych Room.
 */
@Dao
public interface MovieDao {

    /**
     * Wstawia film do bazy danych. Jeśli film o tym samym ID już istnieje, zostaje zastąpiony.
     * 
     * @param movie Obiekt encji filmu do wstawienia.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MovieEntity movie);

    /**
     * Usuwa określony film z bazy danych.
     * 
     * @param movie Obiekt encji filmu do usunięcia.
     */
    @Delete
    void deleteMovie(MovieEntity movie);

    /**
     * Pobiera listę filmów przefiltrowaną według statusu obejrzenia.
     * 
     * @param isWatched Status określający, czy szukać filmów obejrzanych (true), czy do obejrzenia (false).
     * @return Obiekt LiveData zawierający listę filmów.
     */
    @Query("SELECT * FROM movies_table WHERE isWatched = :isWatched")
    LiveData<List<MovieEntity>> getMoviesByWatchStatus(boolean isWatched);

    /**
     * Usuwa film z bazy danych na podstawie jego identyfikatora.
     * 
     * @param movieId Unikalny identyfikator filmu.
     */
    @Query("DELETE FROM movies_table WHERE id = :movieId")
    void deleteMovieById(int movieId);

    /**
     * Pobiera pojedynczy film z bazy danych na podstawie jego identyfikatora.
     * 
     * @param id Unikalny identyfikator filmu.
     * @return Obiekt encji filmu lub null, jeśli nie znaleziono filmu o podanym ID.
     */
    @Query("SELECT * FROM movies_table WHERE id = :id LIMIT 1")
    MovieEntity getMovieById(int id);
}
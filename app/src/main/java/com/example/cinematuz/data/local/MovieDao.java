package com.example.cinematuz.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovieDao {

    // Dodaje film lub aktualizuje, jeśli już taki istnieje
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MovieEntity movie);

    // Usuwa konkretny film z biblioteki
    @Delete
    void deleteMovie(MovieEntity movie);

    // Pobiera filmy w zależności od zakładki
    @Query("SELECT * FROM movies_table WHERE isWatched = :isWatched")
    LiveData<List<MovieEntity>> getMoviesByWatchStatus(boolean isWatched);

    // Pobieranie bezpośrednie do sprawdzenia w tle
    @Query("SELECT * FROM movies_table WHERE id = :id LIMIT 1")
    MovieEntity getMovieById(int id);

    // Pobiera wszystkie zapisane pozycje
    @Query("SELECT * FROM movies_table")
    LiveData<List<MovieEntity>> getAllMovies();
}

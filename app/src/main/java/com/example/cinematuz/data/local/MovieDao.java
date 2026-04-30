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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMovie(MovieEntity movie);

    @Delete
    void deleteMovie(MovieEntity movie);

    @Query("SELECT * FROM movies_table WHERE isWatched = :isWatched")
    LiveData<List<MovieEntity>> getMoviesByWatchStatus(boolean isWatched);

    // Szybkie usunięcie po ID
    @Query("DELETE FROM movies_table WHERE id = :movieId")
    void deleteMovieById(int movieId);

    // Sprawdza, czy film jest w bazie (zwraca null, jeśli nie ma)
    @Query("SELECT * FROM movies_table WHERE id = :id LIMIT 1")
    MovieEntity getMovieById(int id);
}
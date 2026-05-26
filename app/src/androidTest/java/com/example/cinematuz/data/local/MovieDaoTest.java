package com.example.cinematuz.data.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cinematuz.utils.LiveDataTestUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MovieDaoTest {

    // Konieczne, aby operacje na LiveData wykonywały się synchronicznie na głównym wątku
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase database;
    private MovieDao movieDao;

    @Before
    public void setup() {
        // Tworzymy bazę danych "w pamięci" (in-memory), więc po zamknięciu testu dane znikną.
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries() // Zezwalamy na zapytania na głównym wątku (Tylko dla testów!)
                .build();
        movieDao = database.movieDao();
    }

    @After
    public void teardown() {
        // Zamykamy bazę po każdym teście
        database.close();
    }

    @Test
    public void insertAndGetMovieById() {
        // Given - Przygotowanie danych
        MovieEntity movie = new MovieEntity(1, "Diuna", "/poster.jpg", "Opis filmu", 8.5, "movie", false, true);

        // When - Wykonanie akcji
        movieDao.insertMovie(movie);
        MovieEntity retrievedMovie = movieDao.getMovieById(1);

        // Then - Weryfikacja
        assertNotNull("Film powinien znajdować się w bazie", retrievedMovie);
        assertEquals("Diuna", retrievedMovie.getTitle());
        assertEquals(8.5, retrievedMovie.getVoteAverage(), 0.0);
    }

    @Test
    public void deleteMovie() {
        // Given
        MovieEntity movie = new MovieEntity(2, "Matrix", "/matrix.jpg", "Opis", 8.7, "movie", true, false);
        movieDao.insertMovie(movie);

        // When
        movieDao.deleteMovie(movie);
        MovieEntity retrievedMovie = movieDao.getMovieById(2);

        // Then
        assertNull("Film powinien zostać usunięty i zwracać null", retrievedMovie);
    }

    @Test
    public void deleteMovieById() {
        // Given
        MovieEntity movie = new MovieEntity(3, "Incepcja", "/incepcja.jpg", "Opis", 8.8, "movie", true, true);
        movieDao.insertMovie(movie);

        // When
        movieDao.deleteMovieById(3);
        MovieEntity retrievedMovie = movieDao.getMovieById(3);

        // Then
        assertNull("Film powinien zostać usunięty po ID i zwracać null", retrievedMovie);
    }

    @Test
    public void getMoviesByWatchStatus() throws InterruptedException {
        // Given
        MovieEntity watchedMovie = new MovieEntity(4, "Film Obejrzany", "/path1", "desc1", 5.0, "movie", true, false);
        MovieEntity notWatchedMovie = new MovieEntity(5, "Film Do Obejrzenia", "/path2", "desc2", 6.0, "movie", false, false);

        movieDao.insertMovie(watchedMovie);
        movieDao.insertMovie(notWatchedMovie);

        // When
        // Pobieramy listę tylko z filmami oznaczonymi jako obejrzane (isWatched = true)
        List<MovieEntity> watchedMoviesList = LiveDataTestUtil.getOrAwaitValue(movieDao.getMoviesByWatchStatus(true));

        // Pobieramy listę z filmami nieobejrzanymi (isWatched = false)
        List<MovieEntity> notWatchedMoviesList = LiveDataTestUtil.getOrAwaitValue(movieDao.getMoviesByWatchStatus(false));

        // Then
        assertEquals("Powinien zwrócić dokładnie 1 obejrzany film", 1, watchedMoviesList.size());
        assertEquals("Tytuł obejrzanego filmu powinien się zgadzać", "Film Obejrzany", watchedMoviesList.get(0).getTitle());

        assertEquals("Powinien zwrócić dokładnie 1 nieobejrzany film", 1, notWatchedMoviesList.size());
        assertEquals("Tytuł nieobejrzanego filmu powinien się zgadzać", "Film Do Obejrzenia", notWatchedMoviesList.get(0).getTitle());
    }

    @Test
    public void insertMovie_replacesOnConflict() {
        // Given
        MovieEntity movieOriginal = new MovieEntity(6, "Avatar", "/avatar.jpg", "Opis", 7.0, "movie", false, false);
        movieDao.insertMovie(movieOriginal);

        // Tworzymy ten sam obiekt (te same ID), ale ze zaktualizowanymi danymi
        MovieEntity movieUpdated = new MovieEntity(6, "Avatar: Istota Wody", "/avatar2.jpg", "Opis zaktualizowany", 8.0, "movie", true, true);

        // When
        movieDao.insertMovie(movieUpdated); // Powinno zadziałać REPLACE
        MovieEntity retrievedMovie = movieDao.getMovieById(6);

        // Then
        assertEquals("Tytuł powinien zostać zaktualizowany", "Avatar: Istota Wody", retrievedMovie.getTitle());
        assertTrue("Status obejrzanego powinien zostać zaktualizowany", retrievedMovie.isWatched());
    }
}
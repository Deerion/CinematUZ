package com.example.cinematuz.data.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cinematuz.data.api.TmdbApi;
import com.example.cinematuz.data.local.MovieDao;
import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import retrofit2.Call;
import retrofit2.Callback;

@RunWith(MockitoJUnitRunner.class)
public class MovieRepositoryTest {

    @Mock
    private TmdbApi mockApi;

    @Mock
    private MovieDao mockDao;

    @Mock
    private Call<ApiResponse<MediaItem>> mockCall;

    private MovieRepository repository;

    @Before
    public void setup() {
        // Teraz możemy użyć konstruktora testowego
        repository = new MovieRepository(mockApi, mockDao);
    }

    @Test
    public void getTrending_callsApiWithCorrectParams() {
        // Given - przygotowujemy callback do mockowania
        Callback<ApiResponse<MediaItem>> mockCallback = mock(Callback.class);

        // Kiedy zawołamy API o trendy, ma nam zwrócić nasz zamockowany Call
        when(mockApi.getTrending("pl-PL", 1)).thenReturn(mockCall);

        // When - Wywołujemy metodę z repozytorium
        repository.getTrending("pl-PL", mockCallback);

        // Then - Weryfikujemy, czy repozytorium faktycznie zapytało Retrofita (API)
        verify(mockApi).getTrending("pl-PL", 1);

        // Sprawdzamy, czy włożono nasz Callback do kolejki (enqueue)
        verify(mockCall).enqueue(mockCallback);
    }

    @Test
    public void insertMovie_callsDaoOnBackgroundThread() throws InterruptedException {
        // Given - Tworzymy przykładowy film
        MovieEntity fakeMovie = new MovieEntity(1, "Diuna", "path", "opis", 8.0, "movie", true, true);

        // When - każemy repozytorium zapisać film
        repository.insertMovie(fakeMovie);

        // Ponieważ metoda używa executorService.execute (wątek w tle),
        // musimy poczekać ułamek sekundy, by wątek zdążył się wykonać w środowisku testowym.
        Thread.sleep(100);

        // Then - Weryfikujemy, czy Repozytorium faktycznie wywołało metodę zapisu z DAO
        verify(mockDao).insertMovie(fakeMovie);
    }
}
package com.example.cinematuz.data.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TmdbApiTest {

    private MockWebServer mockWebServer;
    private TmdbApi tmdbApi;

    @Before
    public void setup() throws Exception {
        // Uruchamiamy lokalny fałszywy serwer
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        // Tworzymy Retrofit podłączony do naszego fałszywego serwera
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        tmdbApi = retrofit.create(TmdbApi.class);
    }

    @After
    public void teardown() throws Exception {
        // Zamykamy serwer po teście
        mockWebServer.shutdown();
    }

    @Test
    public void getTrending_ReturnsSuccessAndCorrectData() throws Exception {
        // Given - każemy serwerowi zwrócić zawartość pliku JSON
        String mockResponseJson = readJsonFromResources("mock_movies_response.json");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(mockResponseJson));

        // When - wywołujemy API zgodnie z sygnaturą z Twojego TmdbApi.java
        // Zakładamy, że klucz API jest dodawany przez Interceptor, więc go tu nie przekazujemy
        Response<ApiResponse<MediaItem>> response = tmdbApi.getTrending("pl-PL", 1).execute();

        // Then - sprawdzamy wyniki
        assertTrue("Odpowiedź powinna być udana (HTTP 200)", response.isSuccessful());

        ApiResponse<MediaItem> apiResponse = response.body();
        assertNotNull("Ciało odpowiedzi nie powinno być nullem", apiResponse);

        // Sprawdzamy generyczną listę wyników
        assertEquals("Liczba wyników powinna wynosić 2", 2, apiResponse.getResults().size());

        // Sprawdzamy pierwszy film (z pliku mock_movies_response.json)
        MediaItem firstMovie = apiResponse.getResults().get(0);
        assertEquals("Tytuł pierwszego filmu powinien wynosić 'Diuna'", "Diuna", firstMovie.getTitle());
        assertEquals("ID powinno wynosić 550", 550, firstMovie.getId());

        // Zabezpieczenie przed podwójnym typem (Double vs Float) w zależności od implementacji MediaItem
        assertTrue("Ocena powinna wynosić 8.0", firstMovie.getVoteAverage() >= 8.0 && firstMovie.getVoteAverage() <= 8.01);
    }

    // Pomocnicza metoda do czytania JSONów
    private String readJsonFromResources(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("Nie znaleziono pliku: " + fileName);
        }
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        }
    }
}
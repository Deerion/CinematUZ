package com.example.cinematuz.data.api;

import com.example.cinematuz.BuildConfig;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

/**
 * Klient Retrofit odpowiedzialny za konfigurację i dostarczanie instancji biblioteki
 * do komunikacji z API The Movie Database (TMDB).
 * Klasa implementuje wzorzec Singleton dla obiektu Retrofit.
 */
public class RetrofitClient {

    /**
     * Adres bazowy dla TMDB API.
     */
    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    /**
     * Instancja obiektu Retrofit (Singleton).
     */
    private static Retrofit retrofit = null;

    /**
     * Zwraca i w razie potrzeby inicjalizuje klienta Retrofit.
     * Konfiguruje OkHttpClient z interceptorami do logowania oraz automatycznego
     * dodawania klucza API (api_key) do każdego zapytania.
     *
     * @return Skonfigurowana instancja Retrofit do obsługi zapytań sieciowych.
     */
    public static Retrofit getClient() {
        if (retrofit == null) {

            // 1. Interceptor do logowania zapytań
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. Interceptor do automatycznego dodawania klucza API (api_key) do każdego zapytania
            Interceptor apiKeyInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    HttpUrl originalHttpUrl = originalRequest.url();

                    // Dodajemy parametr "api_key" używając wartości z BuildConfig
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                            .build();

                    // Budujemy nowe zapytanie z nowym adresem URL
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .url(url);

                    Request newRequest = requestBuilder.build();
                    return chain.proceed(newRequest);
                }
            };

            // 3. Dodajemy oba interceptory do klienta OkHttp
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(apiKeyInterceptor) // Dodaje klucz API
                    .addInterceptor(loggingInterceptor) // Loguje zapytania
                    .build();

            // 4. Budujemy Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

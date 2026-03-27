package com.example.cinematuz.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.remote.RetrofitClient;
import com.example.cinematuz.data.remote.TmdbApi;
import com.example.cinematuz.ui.adapters.MovieGridAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.example.cinematuz.data.models.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // Widoki
    private RecyclerView rvTrending;
    private MovieGridAdapter adapter;
    private MaterialCardView cardSearch;

    // Przyciski filtrów
    private MaterialButton btnFilterAll, btnFilterMovies, btnFilterTv;

    // Widoki Hero
    private ImageView ivHeroPoster;
    private TextView tvHeroTitle, tvHeroSubtitle, tvHeroRating;
    private MaterialButton btnWatch, btnDetails;

    // Dane z API
    private List<MediaItem> allTrendingItems = new ArrayList<>();
    private MediaItem currentHeroItem = null;

    public HomeFragment() {
        // Wymagany pusty konstruktor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        fetchTrendingData();
    }

    private void initViews(View view) {
        cardSearch = view.findViewById(R.id.card_search);

        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterMovies = view.findViewById(R.id.btn_filter_movies);
        btnFilterTv = view.findViewById(R.id.btn_filter_tv);

        ivHeroPoster = view.findViewById(R.id.iv_hero_poster);
        tvHeroTitle = view.findViewById(R.id.tv_hero_title);
        tvHeroSubtitle = view.findViewById(R.id.tv_hero_subtitle);
        tvHeroRating = view.findViewById(R.id.tv_hero_rating);
        btnWatch = view.findViewById(R.id.btn_watch);
        btnDetails = view.findViewById(R.id.btn_details);

        rvTrending = view.findViewById(R.id.rv_trending);
    }

    private void setupRecyclerView() {
        if (rvTrending == null) return;

        adapter = new MovieGridAdapter(item -> {
            Toast.makeText(getContext(), "Kliknięto: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });

        rvTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvTrending.setAdapter(adapter);
    }

    private void setupListeners() {
        cardSearch.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Otwieram wyszukiwarkę", Toast.LENGTH_SHORT).show();
        });

        btnFilterAll.setOnClickListener(v -> filterList("all"));
        btnFilterMovies.setOnClickListener(v -> filterList("movie"));
        btnFilterTv.setOnClickListener(v -> filterList("tv"));

        btnDetails.setOnClickListener(v -> {
            if (currentHeroItem != null) {
                Toast.makeText(getContext(), "Szczegóły: " + currentHeroItem.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        btnWatch.setOnClickListener(v -> {
            if (currentHeroItem != null) {
                int mediaId = currentHeroItem.getId();
                String mediaType = currentHeroItem.getMediaType();
                TmdbApi api = RetrofitClient.getClient().create(TmdbApi.class);

                // TMDB często nie ma zwiastunów po polsku, więc najbezpieczniej prosić o angielskie ("en-US")
                // lub pozwolić API zwrócić to, co ma domyślnie.
                Call<ApiResponse<Video>> call;
                if ("tv".equals(mediaType)) {
                    call = api.getTvVideos(mediaId, "en-US");
                } else {
                    call = api.getMovieVideos(mediaId, "en-US");
                }

                call.enqueue(new Callback<ApiResponse<Video>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Video>> call, @NonNull Response<ApiResponse<Video>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Video> videos = response.body().getResults();
                            String youtubeKey = null;

                            // Szukamy oficjalnego zwiastuna z YouTube
                            for (Video video : videos) {
                                if ("YouTube".equals(video.getSite()) && "Trailer".equals(video.getType())) {
                                    youtubeKey = video.getKey();
                                    break; // Znaleźliśmy, przerywamy pętlę
                                }
                            }

                            // Jeśli nie ma typu "Trailer", bierzemy cokolwiek z YouTube (np. Teaser)
                            if (youtubeKey == null && !videos.isEmpty()) {
                                for (Video video : videos) {
                                    if ("YouTube".equals(video.getSite())) {
                                        youtubeKey = video.getKey();
                                        break;
                                    }
                                }
                            }

                            if (youtubeKey != null) {
                                openYouTubeTrailer(youtubeKey);
                            } else {
                                Toast.makeText(getContext(), "Brak zwiastuna dla tej produkcji", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Video>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Błąd pobierania zwiastuna", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void fetchTrendingData() {
        TmdbApi api = RetrofitClient.getClient().create(TmdbApi.class);

        Call<ApiResponse<MediaItem>> call = api.getTrending("pl-PL", 1);
        call.enqueue(new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MediaItem>> call, @NonNull Response<ApiResponse<MediaItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTrendingItems = response.body().getResults();
                    if (!allTrendingItems.isEmpty()) {
                        // Inicjalne załadowanie widoku z filtrem "wszystkie"
                        filterList("all");
                    }
                } else {
                    Toast.makeText(getContext(), "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<MediaItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                Toast.makeText(getContext(), "Brak połączenia z siecią", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterList(String mediaType) {
        if (allTrendingItems == null || allTrendingItems.isEmpty() || adapter == null) return;

        // Aktualizujemy wygląd przycisków w UI
        updateFilterButtonsUi(mediaType);

        List<MediaItem> filteredItems = new ArrayList<>();
        MediaItem newHeroItem = null;

        // Przechodzimy przez całą listę pobraną z API
        for (MediaItem item : allTrendingItems) {
            // Sprawdzamy, czy element pasuje do wybranego filtru
            boolean matchesFilter = mediaType.equals("all") || mediaType.equals(item.getMediaType());

            if (matchesFilter) {
                if (newHeroItem == null) {
                    // Pierwszy pasujący element staje się nowym ekranem Hero!
                    newHeroItem = item;
                } else {
                    // Pozostałe pasujące elementy dodajemy do listy w siatce
                    filteredItems.add(item);
                }
            }
        }

        // Zasilamy widok Hero nowymi danymi
        if (newHeroItem != null) {
            setupHeroItem(newHeroItem);
        }

        // Zasilamy adapter nową listą
        adapter.submitList(filteredItems);
    }

    private void openYouTubeTrailer(String videoKey) {
        // Natywny intent dla aplikacji YouTube
        Intent appIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("vnd.youtube:" + videoKey));
        // Zwykły link przeglądarkowy jako fallback
        Intent webIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com/watch?v=" + videoKey));

        try {
            startActivity(appIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private void setupHeroItem(MediaItem item) {
        currentHeroItem = item;

        tvHeroTitle.setText(item.getTitle());
        tvHeroSubtitle.setText(item.getOverview() != null ? item.getOverview() : "");
        tvHeroRating.setText(String.format(Locale.getDefault(), "%.1f", item.getVoteAverage()));

        if (item.getPosterPath() != null) {
            String posterUrl = "https://image.tmdb.org/t/p/w780" + item.getPosterPath();
            Glide.with(this)
                    .load(posterUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivHeroPoster);
        }
    }


    private void updateFilterButtonsUi(String selectedType) {
        // Pomocnicza funkcja pobierająca odpowiednie kolory z motywu bieżącego (Light/Dark)
        int colorPrimary = getThemeColor(com.google.android.material.R.attr.colorPrimary);
        int colorOnPrimary = getThemeColor(com.google.android.material.R.attr.colorOnPrimary);
        int colorSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorSurfaceVariant);
        int colorOnSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);

        // Resetowanie wszystkich przycisków do trybu "wyłączonego" (Outline)
        setButtonToInactive(btnFilterAll, colorSurfaceVariant, colorOnSurfaceVariant);
        setButtonToInactive(btnFilterMovies, colorSurfaceVariant, colorOnSurfaceVariant);
        setButtonToInactive(btnFilterTv, colorSurfaceVariant, colorOnSurfaceVariant);

        // Aktywowanie klikniętego przycisku (Wypełnienie)
        if (selectedType.equals("all")) {
            setButtonToActive(btnFilterAll, colorPrimary, colorOnPrimary);
        } else if (selectedType.equals("movie")) {
            setButtonToActive(btnFilterMovies, colorPrimary, colorOnPrimary);
        } else if (selectedType.equals("tv")) {
            setButtonToActive(btnFilterTv, colorPrimary, colorOnPrimary);
        }
    }

    private void setButtonToActive(MaterialButton button, int bgColor, int textColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        button.setTextColor(textColor);
        button.setStrokeWidth(0); // Usuń obramowanie
    }

    private void setButtonToInactive(MaterialButton button, int strokeColor, int textColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent)));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setStrokeWidth(3); // Dodaj obramowanie
    }

    private int getThemeColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }
}
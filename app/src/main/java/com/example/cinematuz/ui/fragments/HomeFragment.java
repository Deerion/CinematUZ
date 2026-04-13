package com.example.cinematuz.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.cinematuz.ui.activities.DetailsActivity;
// IMPORT MODALA FILTRÓW:
import com.example.cinematuz.ui.fragments.FilterBottomSheetFragment;

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
    private View btnFilter; // DODANE: Przycisk wywołujący filtry

    // Empty State i Szkielety (Shimmery)
    private LinearLayout layoutEmptyTrending;
    private TextView tvEmptyText;
    private View skeletonHero;
    private View skeletonTrending;
    private TextView tvPopularHeader; // DODANE: Do kontrolowania napisu "POPULARNE"

    // Widoki Hero
    private ImageView ivHeroPoster;
    private TextView tvHeroTitle, tvHeroSubtitle, tvHeroRating;
    private MaterialButton btnWatch, btnDetails;
    private View layoutHeroMovie;

    // Dane z API
    private List<MediaItem> allTrendingItems = new ArrayList<>();
    private MediaItem currentHeroItem = null;

    private MaterialButton btnFilterRecommended, btnFilterToWatch, btnFilterWatched;
    private CheckBox cbMovies, cbTvSeries;
    private String currentTab = "recommended";

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

        // Pokaż szkielety przed startem pobierania
        showSkeletons();
        fetchTrendingData();
    }

    private void initViews(View view) {
        cardSearch = view.findViewById(R.id.card_search);

        // DODANE: Znajdź przycisk filtra (Załóżmy, że w XML nadałaś mu id btn_filter)
        btnFilter = view.findViewById(R.id.btn_filter);

        btnFilterRecommended = view.findViewById(R.id.btn_filter_recommended);
        btnFilterToWatch = view.findViewById(R.id.btn_filter_to_watch);
        btnFilterWatched = view.findViewById(R.id.btn_filter_watched);

        cbMovies = view.findViewById(R.id.cb_movies);
        cbTvSeries = view.findViewById(R.id.cb_tv_series);

        layoutHeroMovie = view.findViewById(R.id.layout_hero_movie);
        ivHeroPoster = view.findViewById(R.id.iv_hero_poster);
        tvHeroTitle = view.findViewById(R.id.tv_hero_title);
        tvHeroSubtitle = view.findViewById(R.id.tv_hero_subtitle);
        tvHeroRating = view.findViewById(R.id.tv_hero_rating);
        btnWatch = view.findViewById(R.id.btn_watch);
        btnDetails = view.findViewById(R.id.btn_details);

        rvTrending = view.findViewById(R.id.rv_trending);

        layoutEmptyTrending = view.findViewById(R.id.layout_empty_trending);
        tvEmptyText = view.findViewById(R.id.tv_empty_text);
        tvPopularHeader = view.findViewById(R.id.tv_popular_header);

        // Podpięcie szkieletów
        skeletonHero = view.findViewById(R.id.layout_skeleton_hero);
        skeletonTrending = view.findViewById(R.id.layout_skeleton_trending);
    }

    private void setupRecyclerView() {
        if (rvTrending == null) return;

        adapter = new MovieGridAdapter(item -> {
            Intent intent = new Intent(getContext(), DetailsActivity.class);
            if (item != null) {
                intent.putExtra("MEDIA_ITEM", item);
                startActivity(intent);
            }
        });

        rvTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvTrending.setAdapter(adapter);
    }

    private void setupListeners() {
        // DODANE: Akcja otwarcia Bottom Sheeta z filtrami
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
                bottomSheet.show(getParentFragmentManager(), "FilterBottomSheetTag");
            });
        }

        cardSearch.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Otwieram wyszukiwarkę", Toast.LENGTH_SHORT).show();
        });

        btnDetails.setOnClickListener(v -> {
            if (currentHeroItem != null) {
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("MEDIA_ITEM", currentHeroItem);
                startActivity(intent);
            }
        });

        btnWatch.setOnClickListener(v -> {
            if (currentHeroItem != null) {
                int mediaId = currentHeroItem.getId();
                String mediaType = currentHeroItem.getMediaType();
                TmdbApi api = RetrofitClient.getClient().create(TmdbApi.class);

                Call<ApiResponse<Video>> call = "tv".equals(mediaType) ?
                        api.getTvVideos(mediaId, "en-US") :
                        api.getMovieVideos(mediaId, "en-US");

                call.enqueue(new Callback<ApiResponse<Video>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Video>> call, @NonNull Response<ApiResponse<Video>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Video> videos = response.body().getResults();
                            String youtubeKey = null;

                            for (Video video : videos) {
                                if ("YouTube".equals(video.getSite()) && "Trailer".equals(video.getType())) {
                                    youtubeKey = video.getKey();
                                    break;
                                }
                            }

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

        btnFilterRecommended.setOnClickListener(v -> switchTab("recommended"));
        btnFilterToWatch.setOnClickListener(v -> switchTab("to_watch"));
        btnFilterWatched.setOnClickListener(v -> switchTab("watched"));

        cbMovies.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !cbTvSeries.isChecked()) {
                cbMovies.setChecked(true); // Blokada odznaczenia obu
                return;
            }
            refreshCurrentList();
        });

        cbTvSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !cbMovies.isChecked()) {
                cbTvSeries.setChecked(true); // Blokada odznaczenia obu
                return;
            }
            refreshCurrentList();
        });
    }

    private void switchTab(String tab) {
        currentTab = tab;
        updateFilterButtonsUi();
        refreshCurrentList();
    }

    private void fetchTrendingData() {
        TmdbApi api = RetrofitClient.getClient().create(TmdbApi.class);

        // Sprawdzamy aktualny język aplikacji
        String currentLang = getResources().getConfiguration().locale.getLanguage();
        String apiLang = currentLang.equals("pl") ? "pl-PL" : "en-US";

        // Przekazujemy dynamiczny język (apiLang) zamiast "pl-PL"
        Call<ApiResponse<MediaItem>> call = api.getTrending(apiLang, 1);

        call.enqueue(new Callback<ApiResponse<MediaItem>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<MediaItem>> call, @NonNull Response<ApiResponse<MediaItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allTrendingItems = response.body().getResults();
                    if (!allTrendingItems.isEmpty()) {
                        switchTab("recommended"); // Automatyczne załadowanie pierwszej zakładki
                    } else {
                        showEmptyState(getString(R.string.error_no_data));
                    }
                } else {
                    showEmptyState(getString(R.string.error_server));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<MediaItem>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
                showEmptyState(getString(R.string.error_network));
                Toast.makeText(getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshCurrentList() {
        if (currentTab.equals("recommended")) {
            applyRecommendedFilters();
        } else {
            loadFromLibrary(currentTab.equals("watched"));
        }
    }

    private void applyRecommendedFilters() {
        // Przywracamy nagłówek "POPULARNE" w zakładce Rekomendowane
        if (tvPopularHeader != null) tvPopularHeader.setVisibility(View.VISIBLE);

        List<MediaItem> filteredItems = new ArrayList<>();
        MediaItem newHeroItem = null;

        boolean showMovies = cbMovies.isChecked();
        boolean showTv = cbTvSeries.isChecked();

        for (MediaItem item : allTrendingItems) {
            boolean isMovie = "movie".equals(item.getMediaType());
            boolean isTv = "tv".equals(item.getMediaType());

            if ((isMovie && showMovies) || (isTv && showTv)) {
                if (newHeroItem == null) {
                    newHeroItem = item;
                } else {
                    filteredItems.add(item);
                }
            }
        }

        // Wyświetlanie danych z poprawnym tłumaczeniem
        if (filteredItems.isEmpty() && newHeroItem == null) {
            showEmptyState(getString(R.string.empty_recommendations));
        } else {
            hideSkeletonsAndShowData();
            if (newHeroItem != null) setupHeroItem(newHeroItem);
            adapter.submitList(filteredItems);
        }
    }

    private void loadFromLibrary(boolean isWatched) {
        // Na razie ukrywamy Hero (biblioteka nie potrzebuje Hero) i szkielety
        if (layoutHeroMovie != null) layoutHeroMovie.setVisibility(View.GONE);
        if (skeletonHero != null) skeletonHero.setVisibility(View.GONE);

        // Ukrywamy nagłówek "POPULARNE" w bibliotece
        if (tvPopularHeader != null) tvPopularHeader.setVisibility(View.GONE);

        // Używamy tłumaczeń dla pustego stanu
        showEmptyState(getString(isWatched ? R.string.empty_library_watched : R.string.empty_library_to_watch));
    }

    // Zarządzanie UI (Szkielety vs Dane)
    private void showSkeletons() {
        if (skeletonHero != null) skeletonHero.setVisibility(View.VISIBLE);
        if (skeletonTrending != null) skeletonTrending.setVisibility(View.VISIBLE);

        if (layoutHeroMovie != null) layoutHeroMovie.setVisibility(View.GONE);
        if (rvTrending != null) rvTrending.setVisibility(View.GONE);
        if (layoutEmptyTrending != null) layoutEmptyTrending.setVisibility(View.GONE);
    }

    private void hideSkeletonsAndShowData() {
        if (skeletonHero != null) skeletonHero.setVisibility(View.GONE);
        if (skeletonTrending != null) skeletonTrending.setVisibility(View.GONE);

        if (layoutEmptyTrending != null) layoutEmptyTrending.setVisibility(View.GONE);

        if (layoutHeroMovie != null) layoutHeroMovie.setVisibility(View.VISIBLE);
        if (rvTrending != null) rvTrending.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        if (skeletonHero != null) skeletonHero.setVisibility(View.GONE);
        if (skeletonTrending != null) skeletonTrending.setVisibility(View.GONE);

        if (layoutEmptyTrending != null) layoutEmptyTrending.setVisibility(View.VISIBLE);
        if (tvEmptyText != null) tvEmptyText.setText(message);

        if (rvTrending != null) rvTrending.setVisibility(View.GONE);
        if (layoutHeroMovie != null) layoutHeroMovie.setVisibility(View.GONE);
    }

    // TA FUNKCJA JUŻ ODPOWIADA ZA PRZEJŚCIE DO PRZEGLĄDARKI / APLIKACJI YOUTUBE
    private void openYouTubeTrailer(String videoKey) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("vnd.youtube:" + videoKey));
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

    private void updateFilterButtonsUi() {
        int colorPrimary = getThemeColor(com.google.android.material.R.attr.colorPrimary);
        int colorOnPrimary = getThemeColor(com.google.android.material.R.attr.colorOnPrimary);
        int colorSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorSurfaceVariant);
        int colorOnSurfaceVariant = getThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant);

        setButtonToInactive(btnFilterRecommended, colorSurfaceVariant, colorOnSurfaceVariant);
        setButtonToInactive(btnFilterToWatch, colorSurfaceVariant, colorOnSurfaceVariant);
        setButtonToInactive(btnFilterWatched, colorSurfaceVariant, colorOnSurfaceVariant);

        if (currentTab.equals("recommended")) {
            setButtonToActive(btnFilterRecommended, colorPrimary, colorOnPrimary);
        } else if (currentTab.equals("to_watch")) {
            setButtonToActive(btnFilterToWatch, colorPrimary, colorOnPrimary);
        } else if (currentTab.equals("watched")) {
            setButtonToActive(btnFilterWatched, colorPrimary, colorOnPrimary);
        }
    }

    private void setButtonToActive(MaterialButton button, int bgColor, int textColor) {
        if (button != null) {
            button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
            button.setTextColor(textColor);
            button.setStrokeWidth(0);
        }
    }

    private void setButtonToInactive(MaterialButton button, int strokeColor, int textColor) {
        if (button != null) {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.transparent)));
            button.setTextColor(textColor);
            button.setStrokeColor(ColorStateList.valueOf(strokeColor));
            button.setStrokeWidth(3);
        }
    }

    private int getThemeColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attrResId, typedValue, true);
        return typedValue.data;
    }
}
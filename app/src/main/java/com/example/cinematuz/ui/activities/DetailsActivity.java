package com.example.cinematuz.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.ApiResponse;
import com.example.cinematuz.data.models.CreditsResponse;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.data.models.Video;
import com.example.cinematuz.data.remote.RetrofitClient;
import com.example.cinematuz.data.remote.TmdbApi;
import com.example.cinematuz.ui.adapters.CastAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailsActivity extends AppCompatActivity {

    private MediaItem mediaItem;
    private TmdbApi tmdbApi;

    private ImageView ivBackdrop;
    private TextView tvTitle, tvYear, tvRating, tvDuration, tvOverview;
    private ChipGroup cgGenres;
    private FloatingActionButton fabPlay;
    private RecyclerView rvCast;
    private CastAdapter castAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mediaItem = (MediaItem) getIntent().getSerializableExtra("MEDIA_ITEM");
        if (mediaItem == null) {
            Toast.makeText(this, "Błąd: Brak danych o filmie.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tmdbApi = RetrofitClient.getClient().create(TmdbApi.class);
        initViews();
        bindBasicData();
        fetchExtraDetails();
    }

    private void initViews() {
        ivBackdrop = findViewById(R.id.iv_backdrop);
        tvTitle = findViewById(R.id.tv_details_title);
        tvYear = findViewById(R.id.tv_details_year);
        tvRating = findViewById(R.id.tv_details_rating);
        tvDuration = findViewById(R.id.tv_details_duration);
        tvOverview = findViewById(R.id.tv_details_overview);
        cgGenres = findViewById(R.id.cg_genres);
        fabPlay = findViewById(R.id.fab_play);
        rvCast = findViewById(R.id.rv_cast);

        castAdapter = new CastAdapter();
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCast.setAdapter(castAdapter);

        fabPlay.setOnClickListener(v -> fetchAndOpenTrailer());

        ImageButton btnBack = findViewById(R.id.btn_back_custom);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void bindBasicData() {
        tvTitle.setText(mediaItem.getTitle());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", mediaItem.getVoteAverage()));

        // Obsługa pustego stanu dla opisu filmu
        if (mediaItem.getOverview() == null || mediaItem.getOverview().trim().isEmpty()) {
            tvOverview.setText("Opis tej produkcji nie jest jeszcze dostępny.");
            tvOverview.setAlpha(0.5f);
        } else {
            tvOverview.setText(mediaItem.getOverview());
            tvOverview.setAlpha(0.8f);
        }

        if (mediaItem.getReleaseDate() != null && mediaItem.getReleaseDate().length() >= 4) {
            tvYear.setText(mediaItem.getReleaseDate().substring(0, 4));
        }

        if (mediaItem.getBackdropPath() != null) {
            Glide.with(this).load("https://image.tmdb.org/t/p/w1280" + mediaItem.getBackdropPath()).into(ivBackdrop);
        }
    }

    private void fetchExtraDetails() {
        tmdbApi.getMovieCredits(mediaItem.getId(), "pl-PL").enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreditsResponse> call, @NonNull Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Obsługa pustego stanu dla obsady
                    if (response.body().getCast() == null || response.body().getCast().isEmpty()) {
                        findViewById(R.id.tv_cast_empty).setVisibility(View.VISIBLE);
                        rvCast.setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.tv_cast_empty).setVisibility(View.GONE);
                        rvCast.setVisibility(View.VISIBLE);
                        castAdapter.setCastList(response.body().getCast());
                    }
                } else {
                    findViewById(R.id.tv_cast_empty).setVisibility(View.VISIBLE);
                    rvCast.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(@NonNull Call<CreditsResponse> call, @NonNull Throwable t) {
                findViewById(R.id.tv_cast_empty).setVisibility(View.VISIBLE);
                rvCast.setVisibility(View.GONE);
            }
        });

        Call<MediaItem> detailsCall = "tv".equals(mediaItem.getMediaType()) ?
                tmdbApi.getTvDetails(mediaItem.getId(), "pl-PL") :
                tmdbApi.getMovieDetails(mediaItem.getId(), "pl-PL");

        detailsCall.enqueue(new Callback<MediaItem>() {
            @Override
            public void onResponse(@NonNull Call<MediaItem> call, @NonNull Response<MediaItem> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MediaItem fullDetails = response.body();
                    if (fullDetails.getRuntime() != null && fullDetails.getRuntime() > 0) {
                        tvDuration.setText(String.format(Locale.getDefault(), "%d min", fullDetails.getRuntime()));
                    } else {
                        tvDuration.setVisibility(View.GONE);
                    }

                    if (fullDetails.getGenres() != null) {
                        cgGenres.removeAllViews();
                        for (MediaItem.Genre g : fullDetails.getGenres()) {
                            // Pompujemy gotowy, ostylowany układ z pliku XML
                            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_genre_chip, cgGenres, false);
                            chip.setText(g.getName().toUpperCase());

                            // Wyłączamy klikalność, ponieważ to są tylko wizualne etykiety (tagi)
                            chip.setCheckable(false);
                            chip.setClickable(false);

                            cgGenres.addView(chip);
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<MediaItem> call, @NonNull Throwable t) {}
        });
    }

    private void fetchAndOpenTrailer() {
        Call<ApiResponse<Video>> call = "tv".equals(mediaItem.getMediaType()) ?
                tmdbApi.getTvVideos(mediaItem.getId(), "en-US") :
                tmdbApi.getMovieVideos(mediaItem.getId(), "en-US");

        call.enqueue(new Callback<ApiResponse<Video>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Video>> call, @NonNull Response<ApiResponse<Video>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getResults();
                    for (Video v : videos) {
                        if ("YouTube".equals(v.getSite()) && "Trailer".equals(v.getType())) {
                            openYouTube(v.getKey());
                            return;
                        }
                    }
                    if (!videos.isEmpty()) openYouTube(videos.get(0).getKey());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Video>> call, @NonNull Throwable t) {}
        });
    }

    private void openYouTube(String key) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key));
        try {
            startActivity(appIntent);
        } catch (Exception e) {
            startActivity(webIntent);
        }
    }
}
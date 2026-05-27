package com.example.cinematuz.ui.fragments.home.details;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.FragmentDetailsBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import java.util.List;
import java.util.Locale;

/**
 * Fragment wyświetlający szczegółowe informacje o wybranym filmie lub serialu.
 * Umożliwia dodawanie do biblioteki, przeglądanie obsady, gatunków oraz uruchamianie trailerów.
 */
public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private DetailsViewModel viewModel;
    private MediaItem mediaItem;

    /**
     * Inicjalizuje dane wejściowe przekazane w argumentach.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mediaItem = (MediaItem) getArguments().getSerializable("MEDIA_ITEM");
    }

    /**
     * Tworzy i zwraca hierarchię widoków powiązaną z fragmentem.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicjalizuje ViewModel, nasłuchiwacze i obserwatorów. Ładuje brakujące dane z API.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(DetailsViewModel.class);

        setupObservers();
        setupListeners();

        String lang = getResources().getConfiguration().locale.getLanguage().equals("pl") ? "pl-PL" : "en-US";

        if (mediaItem != null) {
            bindBasicInfo(mediaItem);
            viewModel.loadData(mediaItem.getId(), mediaItem.getMediaType(), lang);
            viewModel.checkLocalMovieState(mediaItem.getId());
        } else if (getArguments() != null && getArguments().containsKey("mediaId")) {
            int id = getArguments().getInt("mediaId");
            String type = getArguments().getString("mediaType", "movie");
            viewModel.loadData(id, type, lang);
            viewModel.checkLocalMovieState(id);
        }
    }

    /**
     * Konfiguruje nasłuchiwacze dla przycisków akcji (powrót, trailer, biblioteka).
     */
    private void setupListeners() {
        binding.btnBackCustom.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.fabPlay.setOnClickListener(v -> viewModel.fetchTrailer(mediaItem.getId(), mediaItem.getMediaType()));

        binding.cardWatchlist.setOnClickListener(v -> {
            if (mediaItem != null) {
                viewModel.toggleLibraryStatus(mediaItem, false); // false = Do obejrzenia
            }
        });

        binding.cardWatched.setOnClickListener(v -> {
            if (mediaItem != null) {
                viewModel.toggleLibraryStatus(mediaItem, true); // true = Obejrzane
            }
        });

        binding.cardFavorite.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Ulubione: do wdrożenia w kolejnym kroku", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Konfiguruje obserwowanie danych z ViewModelu (obsada, detale, trailery, stan lokalny).
     */
    private void setupObservers() {
        viewModel.cast.observe(getViewLifecycleOwner(), list -> {
            boolean isEmpty = list == null || list.isEmpty();
            binding.tvCastEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.rvCast.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            if (!isEmpty) {
                CastAdapter adapter = new CastAdapter();
                binding.rvCast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.rvCast.setAdapter(adapter);
                adapter.setCastList(list);
            }
        });

        viewModel.fullDetails.observe(getViewLifecycleOwner(), details -> {
            if (details == null) return;
            bindBasicInfo(details);
            bindGenres(details.getGenres());
        });

        viewModel.trailerKey.observe(getViewLifecycleOwner(), this::openYoutube);

        viewModel.localMovieState.observe(getViewLifecycleOwner(), savedMovie -> {
            int colorSurfaceVariant = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurfaceVariant, 0);
            int colorPrimaryContainer = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimaryContainer, 0);

            if (savedMovie == null) {
                binding.cardWatchlist.setCardBackgroundColor(ColorStateList.valueOf(colorSurfaceVariant));
                binding.cardWatched.setCardBackgroundColor(ColorStateList.valueOf(colorSurfaceVariant));
            } else {
                if (savedMovie.isWatched()) {
                    binding.cardWatched.setCardBackgroundColor(ColorStateList.valueOf(colorPrimaryContainer));
                    binding.cardWatchlist.setCardBackgroundColor(ColorStateList.valueOf(colorSurfaceVariant));
                } else {
                    binding.cardWatchlist.setCardBackgroundColor(ColorStateList.valueOf(colorPrimaryContainer));
                    binding.cardWatched.setCardBackgroundColor(ColorStateList.valueOf(colorSurfaceVariant));
                }
            }
        });
    }

    /**
     * Wiąże podstawowe informacje o filmie z widokami.
     * 
     * @param item Obiekt mediów zawierający dane.
     */
    private void bindBasicInfo(MediaItem item) {
        binding.tvDetailsTitle.setText(orFallback(item.getTitle(), R.string.details_empty_title));
        binding.tvDetailsOverview.setText(orFallback(item.getOverview(), R.string.details_empty_overview));
        binding.tvDetailsYear.setText(getYearText(item.getReleaseDate()));
        binding.tvDetailsRating.setText(getRatingText(item.getVoteAverage()));
        binding.tvDetailsDuration.setText(getDurationText(item.getRuntime()));

        if (item.getBackdropPath() != null && !item.getBackdropPath().trim().isEmpty()) {
            Glide.with(this)
                    .load("https://image.tmdb.org/t/p/w1280" + item.getBackdropPath())
                    .placeholder(R.drawable.hero_cinema)
                    .error(R.drawable.hero_cinema)
                    .into(binding.ivBackdrop);
        } else {
            Glide.with(this)
                    .load(R.drawable.hero_cinema)
                    .into(binding.ivBackdrop);
        }

        bindGenres(item.getGenres());
    }

    /**
     * Dynamicznie tworzy i wyświetla "chipy" z gatunkami.
     * 
     * @param genres Lista gatunków.
     */
    private void bindGenres(List<MediaItem.Genre> genres) {
        binding.cgGenres.removeAllViews();
        int colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, 0);

        if (genres == null || genres.isEmpty()) {
            Chip emptyChip = new Chip(getContext());
            emptyChip.setText(R.string.details_empty_genres);
            emptyChip.setClickable(false);
            emptyChip.setCheckable(false);
            emptyChip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 12)));
            emptyChip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 25)));
            emptyChip.setChipStrokeWidth(1f);
            emptyChip.setTextColor(ColorUtils.setAlphaComponent(colorPrimary, 200));
            binding.cgGenres.addView(emptyChip);
            return;
        }

        for (MediaItem.Genre g : genres) {
            Chip chip = new Chip(getContext());
            chip.setText(g.getName());
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 51)));
            chip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 102)));
            chip.setChipStrokeWidth(1f);
            chip.setTextColor(colorPrimary);
            binding.cgGenres.addView(chip);
        }
    }

    /** Formatuje datę wydania do samego roku. */
    private String getYearText(String releaseDate) {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return getString(R.string.details_empty_year);
    }

    /** Formatuje ocenę numeryczną. */
    private String getRatingText(double voteAverage) {
        if (voteAverage > 0d) {
            return String.format(Locale.getDefault(), "%.1f", voteAverage);
        }
        return getString(R.string.details_empty_rating);
    }

    /** Formatuje czas trwania. */
    private String getDurationText(Integer runtime) {
        if (runtime != null && runtime > 0) {
            return runtime + " min";
        }
        return getString(R.string.details_empty_duration);
    }

    /** Zwraca wartość lub fallback, jeśli wartość jest pusta. */
    private String orFallback(String value, int fallbackRes) {
        if (value == null || value.trim().isEmpty()) {
            return getString(fallbackRes);
        }
        return value;
    }

    /**
     * Otwiera trailer filmu w aplikacji YouTube lub przeglądarce.
     * 
     * @param key Klucz wideo z serwisu YouTube.
     */
    private void openYoutube(String key) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + key)));
    }
}
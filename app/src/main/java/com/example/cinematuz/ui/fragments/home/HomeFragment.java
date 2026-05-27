package com.example.cinematuz.ui.fragments.home;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.FragmentHomeBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import java.util.Locale;

/**
 * Fragment wyświetlający ekran główny aplikacji.
 * Zawiera sekcję "Hero" z polecanym filmem, wyszukiwarkę oraz listę trendujących filmów i seriali
 * z możliwością filtrowania.
 */
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private MovieGridAdapter adapter;
    private String currentFilter = "all";

    /**
     * Tworzy i zwraca hierarchię widoków powiązaną z fragmentem.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Inicjalizuje ViewModel, widoki, nasłuchiwacze i obserwatorów po utworzeniu widoku.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(HomeViewModel.class);

        setupRecyclerView();
        setupInitialState();
        setupListeners();
        setupObservers();

        applyFilter(currentFilter, false);

        if (viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty()) {
            String lang = getResources().getConfiguration().locale.getLanguage().equals("pl") ? "pl-PL" : "en-US";
            viewModel.fetchTrending(lang);
        }
    }

    /**
     * Konfiguruje RecyclerView do wyświetlania siatki filmów.
     */
    private void setupRecyclerView() {
        adapter = new MovieGridAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", item);
            Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
        });

        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        binding.rvTrending.setHasFixedSize(true);
        binding.rvTrending.setItemViewCacheSize(20);
        binding.rvTrending.setDrawingCacheEnabled(true);
        binding.rvTrending.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        binding.rvTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvTrending.setAdapter(adapter);
    }

    /**
     * Ustawia początkową widoczność elementów interfejsu.
     */
    private void setupInitialState() {
        binding.rvTrending.setVisibility(View.GONE);
        binding.layoutEmptyTrending.setVisibility(View.GONE);
        binding.layoutSkeletonHero.getRoot().setVisibility(View.GONE);
        binding.layoutSkeletonTrending.getRoot().setVisibility(View.GONE);
    }

    /**
     * Natychmiast ukrywa widoki szkieletowe (skeleton screens).
     */
    private void hideSkeletonsInstantly() {
        if (binding == null) return;
        binding.layoutSkeletonHero.getRoot().setVisibility(View.GONE);
        binding.layoutSkeletonTrending.getRoot().setVisibility(View.GONE);
    }

    /**
     * Zarządza widocznością stanu pustego dla sekcji trendów.
     * 
     * @param show Prawda, jeśli stan pusty ma być widoczny.
     */
    private void showEmptyTrendingState(boolean show) {
        if (binding == null) return;
        binding.layoutEmptyTrending.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.rvTrending.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    /**
     * Konfiguruje obserwowanie danych z ViewModelu.
     */
    private void setupObservers() {
        viewModel.heroItem.observe(getViewLifecycleOwner(), this::updateHeroUi);

        viewModel.trendingList.observe(getViewLifecycleOwner(), list -> {
            hideSkeletonsInstantly();
            Boolean loading = viewModel.isLoading.getValue();
            boolean isEmpty = list == null || list.isEmpty();
            if (loading == null || !loading) {
                showEmptyTrendingState(isEmpty);
            }
            if (!isEmpty) adapter.submitList(list);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading != null && loading) {
                binding.layoutSkeletonHero.getRoot().setVisibility(View.VISIBLE);
                binding.layoutSkeletonTrending.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEmptyTrending.setVisibility(View.GONE);
                binding.layoutHeroMovie.getRoot().setVisibility(View.GONE);
            } else if (!loading) {
                hideSkeletonsInstantly();
                boolean isEmpty = viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty();
                showEmptyTrendingState(isEmpty);
            }
        });
    }

    /**
     * Konfiguruje nasłuchiwacze kliknięć dla przycisków i elementów interaktywnych.
     */
    private void setupListeners() {
        binding.btnFilterAll.setOnClickListener(v -> applyFilter("all", true));
        binding.btnFilterMovies.setOnClickListener(v -> applyFilter("movie", true));
        binding.btnFilterTv.setOnClickListener(v -> applyFilter("tv", true));

        binding.layoutHeroMovie.btnDetails.setOnClickListener(v -> {
            MediaItem hero = viewModel.heroItem.getValue();
            if (hero != null) {
                Bundle b = new Bundle();
                b.putSerializable("MEDIA_ITEM", hero);
                Navigation.findNavController(v).navigate(R.id.detailsFragment, b);
            }
        });

        binding.cardSearch.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.searchFragment));
        binding.tvSearchBar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.searchFragment));
    }

    /**
     * Aktualizuje interfejs użytkownika sekcji Hero na podstawie dostarczonego elementu mediów.
     * 
     * @param item Element do wyświetlenia w sekcji Hero.
     */
    private void updateHeroUi(MediaItem item) {
        if (binding == null) return;

        if (item == null) {
            binding.layoutHeroMovie.getRoot().setVisibility(View.GONE);
            return;
        }

        binding.layoutHeroMovie.getRoot().setVisibility(View.VISIBLE);

        String heroTitle = item.getTitle();
        String heroOverview = item.getOverview();
        double heroRating = item.getVoteAverage();
        String posterPath = item.getPosterPath();

        binding.layoutHeroMovie.tvHeroTitle.setText(orFallback(heroTitle, R.string.hero_empty_title));
        binding.layoutHeroMovie.tvHeroSubtitle.setText(orFallback(heroOverview, R.string.hero_empty_overview));
        binding.layoutHeroMovie.tvHeroRating.setText(getHeroRatingText(heroRating));

        if (posterPath != null && !posterPath.trim().isEmpty()) {
            Glide.with(this)
                    .load("https://image.tmdb.org/t/p/w780" + posterPath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .placeholder(R.drawable.hero_cinema)
                    .error(R.drawable.hero_cinema)
                    .into(binding.layoutHeroMovie.ivHeroPoster);
        } else {
            Glide.with(this)
                    .load(R.drawable.hero_cinema)
                    .into(binding.layoutHeroMovie.ivHeroPoster);
        }
    }

    /**
     * Formatuje tekst oceny dla sekcji Hero.
     * 
     * @param rating Ocena numeryczna.
     * @return Sformatowany ciąg znaków z oceną.
     */
    private String getHeroRatingText(double rating) {
        if (rating > 0d) return String.format(Locale.getDefault(), "%.1f", rating);
        return getString(R.string.hero_empty_rating);
    }

    /**
     * Zwraca wartość lub tekst zastępczy, jeśli wartość jest pusta.
     * 
     * @param value Sprawdzana wartość.
     * @param fallbackRes Zasób tekstowy używany jako fallback.
     * @return Ciąg znaków do wyświetlenia.
     */
    private String orFallback(String value, int fallbackRes) {
        return (value == null || value.trim().isEmpty()) ? getString(fallbackRes) : value;
    }

    /**
     * Nakłada wybrany filtr na listę trendów.
     * 
     * @param filter Klucz filtra ("all", "movie", "tv").
     * @param updateData Czy odświeżyć dane w ViewModelu.
     */
    private void applyFilter(String filter, boolean updateData) {
        currentFilter = filter;
        if (updateData) viewModel.applyFilter(filter);

        updateButtonStyle(binding.btnFilterAll, "all".equals(filter));
        updateButtonStyle(binding.btnFilterMovies, "movie".equals(filter));
        updateButtonStyle(binding.btnFilterTv, "tv".equals(filter));
    }

    /**
     * Aktualizuje wygląd przycisku filtra w zależności od tego, czy jest wybrany.
     * 
     * @param button Przycisk do zaktualizowania.
     * @param isSelected Czy przycisk jest aktualnie wybrany.
     */
    private void updateButtonStyle(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(ColorStateList.valueOf(MaterialColors.getColor(button, com.google.android.material.R.attr.colorPrimary)));
            button.setTextColor(MaterialColors.getColor(button, com.google.android.material.R.attr.colorOnPrimary));
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(MaterialColors.getColor(button, com.google.android.material.R.attr.colorOnSurfaceVariant));
            button.setStrokeColor(ColorStateList.valueOf(MaterialColors.getColor(button, com.google.android.material.R.attr.colorOutline)));
            button.setStrokeWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
        }
    }

    /**
     * Czyści binding przy niszczeniu widoku fragmentu.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
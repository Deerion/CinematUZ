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

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private MovieGridAdapter adapter;
    private View rootView; // Caching widoku powraca!
    private String currentFilter = "all";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Wracamy do Twojego patentu. Generujemy ciężki widok tylko za pierwszym wejściem.
        if (rootView == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();

            // Te rzeczy też ustalamy tylko raz na całe życie aplikacji!
            setupRecyclerView();
            setupInitialState();
            setupListeners();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupObservers();
        applyFilter(currentFilter, false);

        // Strzał do API tylko za pierwszym razem
        if (viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty()) {
            String lang = getResources().getConfiguration().locale.getLanguage().equals("pl") ? "pl-PL" : "en-US";
            viewModel.fetchTrending(lang);
        }
    }

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

    private void setupInitialState() {
        binding.rvTrending.setVisibility(View.GONE);
        binding.layoutEmptyTrending.setVisibility(View.GONE);
    }

    private void hideSkeletonsInstantly() {
        if (binding == null) return;
        binding.layoutSkeletonHero.getRoot().setVisibility(View.GONE);
        binding.layoutSkeletonTrending.getRoot().setVisibility(View.GONE);
        binding.layoutHeroMovie.getRoot().setVisibility(View.VISIBLE);
    }

    private void showEmptyTrendingState(boolean show) {
        binding.layoutEmptyTrending.setVisibility(show ? View.VISIBLE : View.GONE);
        // Hero zostaje widoczny i pokazuje fallbacki nawet przy braku danych.
        binding.layoutHeroMovie.getRoot().setVisibility(View.VISIBLE);
        binding.rvTrending.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setupObservers() {
        viewModel.heroItem.observe(getViewLifecycleOwner(), this::updateHeroUi);

        viewModel.trendingList.observe(getViewLifecycleOwner(), list -> {
            hideSkeletonsInstantly();
            boolean isEmpty = list == null || list.isEmpty();
            showEmptyTrendingState(isEmpty);
            if (!isEmpty) {
                adapter.submitList(list);
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading && (viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty())) {
                binding.layoutSkeletonHero.getRoot().setVisibility(View.VISIBLE);
                binding.layoutSkeletonTrending.getRoot().setVisibility(View.VISIBLE);
                binding.layoutEmptyTrending.setVisibility(View.GONE);
            } else if (!loading) {
                hideSkeletonsInstantly();
                boolean isEmpty = viewModel.trendingList.getValue() == null || viewModel.trendingList.getValue().isEmpty();
                showEmptyTrendingState(isEmpty);
            }
        });
    }

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

        // --- NOWE: Przejście do wyszukiwarki ---
        binding.cardSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.searchFragment);
        });

        // Na wszelki wypadek przypinamy też do samego tekstu, by kliknięcie było bezbłędne
        binding.tvSearchBar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.searchFragment);
        });
    }

    private void updateHeroUi(MediaItem item) {
        if (binding == null) return;

        String heroTitle = item != null ? item.getTitle() : null;
        String heroOverview = item != null ? item.getOverview() : null;
        double heroRating = item != null ? item.getVoteAverage() : 0d;
        String posterPath = item != null ? item.getPosterPath() : null;

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

    private String getHeroRatingText(double rating) {
        if (rating > 0d) {
            return String.format(Locale.getDefault(), "%.1f", rating);
        }
        return getString(R.string.hero_empty_rating);
    }

    private String orFallback(String value, int fallbackRes) {
        if (value == null || value.trim().isEmpty()) {
            return getString(fallbackRes);
        }
        return value;
    }

    private void applyFilter(String filter, boolean updateData) {
        currentFilter = filter;
        if (updateData) {
            viewModel.applyFilter(filter);
        }

        updateButtonStyle(binding.btnFilterAll, "all".equals(filter));
        updateButtonStyle(binding.btnFilterMovies, "movie".equals(filter));
        updateButtonStyle(binding.btnFilterTv, "tv".equals(filter));
    }

    private void updateButtonStyle(MaterialButton button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundTintList(ColorStateList.valueOf(MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorPrimary)));
            button.setTextColor(MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorOnPrimary));
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorOnSurfaceVariant));
            button.setStrokeColor(ColorStateList.valueOf(MaterialColors.getColor(requireView(), com.google.android.material.R.attr.colorOutline)));
            button.setStrokeWidth(Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // CELOWO PUSTE: Zostawiamy referencje do rootView.
        // Odcięcie ich tutaj wymusiłoby generowanie widoku od nowa (i wywołało te sekundowe lagi).
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // BEZPIECZEŃSTWO: Niszczymy referencje w momencie niszczenia całego Fragmentu (np. przy zamknięciu aplikacji lub wycieku Activity).
        // Dzięki temu osiągamy natychmiastowe ładowanie, unikając równocześnie groźnych wycieków pamięci!
        binding = null;
        rootView = null;
    }
}
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

    private String currentTab = "recommended";

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
        switchTab(currentTab);

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
        viewModel.heroItem.observe(getViewLifecycleOwner(), item -> {
            if ("recommended".equals(currentTab)) {
                updateHeroUi(item);
            }
        });

        viewModel.trendingList.observe(getViewLifecycleOwner(), list -> {
            if ("recommended".equals(currentTab)) {
                hideSkeletonsInstantly();
                boolean isEmpty = list == null || list.isEmpty();
                showEmptyTrendingState(isEmpty);
                if (!isEmpty) {
                    adapter.submitList(list);
                }
            }
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (!"recommended".equals(currentTab)) return;

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
        // Przełączanie zakładek
        binding.btnFilterRecommended.setOnClickListener(v -> switchTab("recommended"));
        binding.btnFilterToWatch.setOnClickListener(v -> switchTab("to_watch"));
        binding.btnFilterWatched.setOnClickListener(v -> switchTab("watched"));

        // Logika checkboxów
        binding.cbMovies.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !binding.cbTvSeries.isChecked()) {
                binding.cbMovies.setChecked(true); // Blokada odznaczenia obu
                return;
            }
            refreshCurrentList();
        });

        binding.cbTvSeries.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && !binding.cbMovies.isChecked()) {
                binding.cbTvSeries.setChecked(true); // Blokada odznaczenia obu
                return;
            }
            refreshCurrentList();
        });

        binding.layoutHeroMovie.btnDetails.setOnClickListener(v -> {
            MediaItem hero = viewModel.heroItem.getValue();
            if (hero != null) {
                Bundle b = new Bundle();
                b.putSerializable("MEDIA_ITEM", hero);
                Navigation.findNavController(v).navigate(R.id.detailsFragment, b);
            }
        });

        // --- Przejście do wyszukiwarki ---
        binding.cardSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.searchFragment);
        });
        binding.tvSearchBar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.searchFragment);
        });
    }

    private void switchTab(String tab) {
        currentTab = tab;
        updateFilterButtonsUi();
        refreshCurrentList();
    }

    private void refreshCurrentList() {
        if ("recommended".equals(currentTab)) {
            binding.tvPopularHeader.setVisibility(View.VISIBLE);

            // Mapujemy zaznaczone checkboxy na filtr, który obsłuży HomeViewModel z GitHuba
            String filter = "all";
            if (binding.cbMovies.isChecked() && !binding.cbTvSeries.isChecked()) filter = "movie";
            else if (!binding.cbMovies.isChecked() && binding.cbTvSeries.isChecked()) filter = "tv";

            viewModel.applyFilter(filter);

            // Przywracamy Hero
            if (viewModel.heroItem.getValue() != null) {
                binding.layoutHeroMovie.getRoot().setVisibility(View.VISIBLE);
                updateHeroUi(viewModel.heroItem.getValue());
            }

        } else {
            // Tryb Biblioteki (Do Obejrzenia / Obejrzane)
            binding.tvPopularHeader.setVisibility(View.GONE);
            binding.layoutHeroMovie.getRoot().setVisibility(View.GONE);
            binding.rvTrending.setVisibility(View.GONE);
            binding.layoutSkeletonHero.getRoot().setVisibility(View.GONE);
            binding.layoutSkeletonTrending.getRoot().setVisibility(View.GONE);

            // Wyświetlenie "Pustego Stanu"
            binding.layoutEmptyTrending.setVisibility(View.VISIBLE);
            if ("watched".equals(currentTab)) {
                binding.tvEmptyText.setText(R.string.empty_library_watched);
            } else {
                binding.tvEmptyText.setText(R.string.empty_library_to_watch);
            }
        }
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

    private void updateFilterButtonsUi() {
        updateButtonStyle(binding.btnFilterRecommended, "recommended".equals(currentTab));
        updateButtonStyle(binding.btnFilterToWatch, "to_watch".equals(currentTab));
        updateButtonStyle(binding.btnFilterWatched, "watched".equals(currentTab));
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
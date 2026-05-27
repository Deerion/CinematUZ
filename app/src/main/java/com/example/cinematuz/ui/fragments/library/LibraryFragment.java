package com.example.cinematuz.ui.fragments.library;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.local.MovieEntity;
import com.example.cinematuz.data.models.MediaItem;
import com.example.cinematuz.databinding.FragmentLibraryBinding;
import com.example.cinematuz.ui.fragments.home.MovieGridAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment wyświetlający bibliotekę użytkownika.
 * Zawiera zakładki "Do obejrzenia" i "Obejrzane" oraz umożliwia filtrowanie treści
 * według typu (filmy/seriale) i zarządzanie listą (usuwanie elementów).
 */
public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private MovieGridAdapter adapter;
    private View rootView;
    private LibraryViewModel viewModel;

    private List<MovieEntity> toWatchList = new ArrayList<>();
    private List<MovieEntity> watchedList = new ArrayList<>();

    private String currentStatus = "to_watch";
    private String currentType = "all";

    /**
     * Tworzy widok fragmentu i konfiguruje podstawowe komponenty RecyclerView.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            binding = FragmentLibraryBinding.inflate(inflater, container, false);
            rootView = binding.getRoot();

            setupRecyclerView();
            setupListeners();
        }
        return rootView;
    }

    /**
     * Inicjalizuje ViewModel i konfiguruje obserwatorów bazy danych.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(LibraryViewModel.class);

        updateTypeButtonsUi();
        setupObservers();
    }

    /**
     * Konfiguruje obserwowanie list filmów z lokalnej bazy danych.
     */
    private void setupObservers() {
        viewModel.getMoviesToWatch().observe(getViewLifecycleOwner(), movies -> {
            toWatchList = movies;
            if ("to_watch".equals(currentStatus)) {
                refreshLibraryList();
            }
        });

        viewModel.getWatchedMovies().observe(getViewLifecycleOwner(), movies -> {
            watchedList = movies;
            if ("watched".equals(currentStatus)) {
                refreshLibraryList();
            }
        });
    }

    /**
     * Odświeża listę wyświetlaną w adapterze na podstawie aktualnych filtrów statusu i typu.
     */
    private void refreshLibraryList() {
        List<MovieEntity> sourceList = "to_watch".equals(currentStatus) ? toWatchList : watchedList;
        List<MediaItem> filteredList = new ArrayList<>();

        for (MovieEntity entity : sourceList) {
            if ("all".equals(currentType) || currentType.equals(entity.getMediaType())) {
                filteredList.add(viewModel.convertToMediaItem(entity));
            }
        }

        adapter.submitList(filteredList);

        if (filteredList.isEmpty()) {
            binding.rvLibrary.setVisibility(View.GONE);
            binding.layoutEmptyLibrary.setVisibility(View.VISIBLE);

            if ("watched".equals(currentStatus)) {
                binding.tvEmptyLibraryText.setText(getString(R.string.empty_library_watched));
            } else {
                binding.tvEmptyLibraryText.setText(getString(R.string.empty_library_to_watch));
            }
        } else {
            binding.rvLibrary.setVisibility(View.VISIBLE);
            binding.layoutEmptyLibrary.setVisibility(View.GONE);
        }
    }

    /**
     * Konfiguruje RecyclerView oraz menu kontekstowe dla elementów listy.
     */
    private void setupRecyclerView() {
        adapter = new MovieGridAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", item);
            Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
        });

        adapter.setOnItemLongClickListener((item, anchorView) -> {
            PopupMenu popup = new PopupMenu(requireContext(), anchorView);
            popup.getMenu().add("Usuń z biblioteki");

            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getTitle().equals("Usuń z biblioteki")) {
                    viewModel.removeFromLibrary(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.rvLibrary.setHasFixedSize(true);
        binding.rvLibrary.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvLibrary.setAdapter(adapter);
    }

    /**
     * Ustawia nasłuchiwacze dla zakładek, przycisków filtrów i akcji.
     */
    private void setupListeners() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatus = (tab.getPosition() == 0) ? "to_watch" : "watched";
                refreshLibraryList();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.btnLibAll.setOnClickListener(v -> changeTypeFilter("all"));
        binding.btnLibMovies.setOnClickListener(v -> changeTypeFilter("movie"));
        binding.btnLibTv.setOnClickListener(v -> changeTypeFilter("tv"));

        binding.btnEmptySearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_start);
        });

        binding.btnFilterLibrary.setOnClickListener(v -> {
            LibraryFilterBottomSheet bottomSheet = new LibraryFilterBottomSheet();
            bottomSheet.show(getChildFragmentManager(), "LibraryFilterBottomSheet");
        });
    }

    /**
     * Zmienia aktualny filtr typu mediów i odświeża widok.
     * 
     * @param type Typ mediów ("all", "movie", "tv").
     */
    private void changeTypeFilter(String type) {
        if (currentType.equals(type)) return;
        currentType = type;
        updateTypeButtonsUi();
        refreshLibraryList();
    }

    /**
     * Aktualizuje wygląd przycisków przełączających typ treści.
     */
    private void updateTypeButtonsUi() {
        updateButtonStyle(binding.btnLibAll, "all".equals(currentType));
        updateButtonStyle(binding.btnLibMovies, "movie".equals(currentType));
        updateButtonStyle(binding.btnLibTv, "tv".equals(currentType));
    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        rootView = null;
    }
}
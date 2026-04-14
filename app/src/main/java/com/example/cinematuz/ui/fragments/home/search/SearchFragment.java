package com.example.cinematuz.ui.fragments.home.search;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.FilterCriteria;
import com.example.cinematuz.ui.fragments.home.FilterBottomSheetFragment;
import com.example.cinematuz.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private SearchResultAdapter adapter;

    private EditText etSearch;
    private ProgressBar progressBar;
    private RecyclerView rvSearchResults;
    private ImageButton btnClearSearch;
    private ImageButton btnSearchBack;
    private ImageView btnOpenFilters;
    private TextView tvFilterBadge;
    private TextView tvSearchEmpty;

    private MaterialButton btnFilterAll, btnFilterMovies, btnFilterTv;
    private SearchResultAdapter.FilterType currentFilter = SearchResultAdapter.FilterType.ALL;
    private FilterCriteria lastAppliedCriteria;
    private boolean updatingFilterFromModal;

    private static final String STATE_LAST_FILTER = "state_last_filter";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        setupObservers();
        setupFilterResultListener();

        if (savedInstanceState != null) {
            lastAppliedCriteria = (FilterCriteria) savedInstanceState.getSerializable(STATE_LAST_FILTER);
        }

        updateFilterBadge();

        setFilter(currentFilter);
        showInitialEmptyState();
        etSearch.requestFocus();

        // --- NOWY KOD: Sprawdzamy czy przyszliśmy tutaj z ikonki filtra na ekranie głównym ---
        if (getArguments() != null && getArguments().getBoolean("open_filters", false)) {
            // Czyścimy flagę, aby przy ewentualnym obrocie ekranu modal nie wyskakiwał w kółko
            getArguments().putBoolean("open_filters", false);

            // Otwieramy modal z filtrami
            openFilterBottomSheet();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_LAST_FILTER, lastAppliedCriteria);
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.searchProgressBar);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnSearchBack = view.findViewById(R.id.btnSearchBack);
        tvSearchEmpty = view.findViewById(R.id.tvSearchEmpty);

        // ID przycisku filtrów w Twoim fragment_search.xml
        btnOpenFilters = view.findViewById(R.id.btn_filter);
        tvFilterBadge = view.findViewById(R.id.tv_filter_badge);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterMovies = view.findViewById(R.id.btnFilterMovies);
        btnFilterTv = view.findViewById(R.id.btnFilterTv);
    }

    private void setupRecyclerView() {
        adapter = new SearchResultAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", item);
            Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearchBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            showInitialEmptyState();
        });

        // Otwieranie BottomSheet z filtrami
        if (btnOpenFilters != null) {
            btnOpenFilters.setOnClickListener(v -> openFilterBottomSheet());
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                if (s.toString().trim().isEmpty()) {
                    adapter.submitList(null);
                    showInitialEmptyState();
                } else {
                    viewModel.onSearchTextChanged(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterAll.setOnClickListener(v -> onTopFilterSelected(SearchResultAdapter.FilterType.ALL));
        btnFilterMovies.setOnClickListener(v -> onTopFilterSelected(SearchResultAdapter.FilterType.MOVIE));
        btnFilterTv.setOnClickListener(v -> onTopFilterSelected(SearchResultAdapter.FilterType.TV));
    }

    /**
     * Kluczowa funkcja odbierająca dane z modalu filtrów
     */
    private void setupFilterResultListener() {
        getChildFragmentManager().setFragmentResultListener("filter_request", getViewLifecycleOwner(), (requestKey, bundle) -> {
            FilterCriteria criteria = (FilterCriteria) bundle.getSerializable("filter_data");
            if (criteria != null) {
                lastAppliedCriteria = copyCriteria(criteria);
                updateFilterBadge();
                String lang = LocaleHelper.getLanguage(requireContext()).equals("pl") ? "pl-PL" : "en-US";
                String currentQuery = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();

                // Wywołujemy zaawansowane filtrowanie w ViewModelu
                viewModel.applyAdvancedFilters(criteria, currentQuery, lang);

                // Górne chipy odzwierciedlają aktualny typ z modala.
                updatingFilterFromModal = true;
                setFilter(mapContentTypeToTopFilter(criteria.contentType));
                updatingFilterFromModal = false;
            }
        });
    }

    private void openFilterBottomSheet() {
        FilterBottomSheetFragment bottomSheet = new FilterBottomSheetFragment();
        if (lastAppliedCriteria != null) {
            Bundle args = new Bundle();
            args.putSerializable(FilterBottomSheetFragment.ARG_INITIAL_FILTER, copyCriteria(lastAppliedCriteria));
            bottomSheet.setArguments(args);
        }
        bottomSheet.show(getChildFragmentManager(), "FilterBottomSheet");
    }

    private void onTopFilterSelected(SearchResultAdapter.FilterType filterType) {
        setFilter(filterType);

        if (updatingFilterFromModal || lastAppliedCriteria == null) {
            return;
        }

        FilterCriteria updated = copyCriteria(lastAppliedCriteria);
        updated.contentType = mapTopFilterToContentType(filterType);
        lastAppliedCriteria = updated;
        updateFilterBadge();

        String lang = LocaleHelper.getLanguage(requireContext()).equals("pl") ? "pl-PL" : "en-US";
        String currentQuery = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
        viewModel.applyAdvancedFilters(updated, currentQuery, lang);
    }

    private SearchResultAdapter.FilterType mapContentTypeToTopFilter(String contentType) {
        if ("movie".equals(contentType)) return SearchResultAdapter.FilterType.MOVIE;
        if ("tv".equals(contentType)) return SearchResultAdapter.FilterType.TV;
        return SearchResultAdapter.FilterType.ALL;
    }

    private String mapTopFilterToContentType(SearchResultAdapter.FilterType filterType) {
        if (filterType == SearchResultAdapter.FilterType.MOVIE) return "movie";
        if (filterType == SearchResultAdapter.FilterType.TV) return "tv";
        return "all";
    }

    private FilterCriteria copyCriteria(FilterCriteria source) {
        if (source == null) return null;
        FilterCriteria copy = new FilterCriteria();
        copy.sortBy = source.sortBy;
        copy.contentType = source.contentType;
        copy.genreIds = source.genreIds == null ? new ArrayList<>() : new ArrayList<>(source.genreIds);
        copy.yearFrom = source.yearFrom;
        copy.yearTo = source.yearTo;
        copy.minRating = source.minRating;
        if (TextUtils.isEmpty(copy.contentType)) {
            copy.contentType = "all";
        }
        return copy;
    }

    private void setFilter(SearchResultAdapter.FilterType filterType) {
        currentFilter = filterType;
        adapter.setFilter(filterType);

        updateButtonStyle(btnFilterAll, filterType == SearchResultAdapter.FilterType.ALL);
        updateButtonStyle(btnFilterMovies, filterType == SearchResultAdapter.FilterType.MOVIE);
        updateButtonStyle(btnFilterTv, filterType == SearchResultAdapter.FilterType.TV);
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

    private void setupObservers() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (progressBar != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), results -> {
            adapter.submitList(results);

            // Logika wyświetlania "pustego stanu"
            boolean isQueryEmpty = etSearch.getText() == null || etSearch.getText().toString().trim().isEmpty();

            if (results == null || results.isEmpty()) {
                if (isQueryEmpty) {
                    // Tutaj możemy być po filtrowaniu (Discover), więc jeśli są wyniki = null, pokazujemy info
                    tvSearchEmpty.setText(R.string.empty_search_results);
                } else {
                    tvSearchEmpty.setText(R.string.empty_search_results);
                }
                tvSearchEmpty.setVisibility(View.VISIBLE);
            } else {
                tvSearchEmpty.setVisibility(View.GONE);
            }
        });
    }

    private void showInitialEmptyState() {
        tvSearchEmpty.setText(R.string.empty_search_initial);
        tvSearchEmpty.setVisibility(View.VISIBLE);
    }

    private void updateFilterBadge() {
        if (tvFilterBadge == null) return;

        int activeCount = getActiveFilterCount(lastAppliedCriteria);
        tvFilterBadge.setVisibility(activeCount > 0 ? View.VISIBLE : View.GONE);
    }

    private int getActiveFilterCount(@Nullable FilterCriteria criteria) {
        if (criteria == null) return 0;

        int active = 0;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        String contentType = TextUtils.isEmpty(criteria.contentType) ? "all" : criteria.contentType;
        String sortBy = TextUtils.isEmpty(criteria.sortBy) ? "popularity.desc" : criteria.sortBy;
        int yearFrom = criteria.yearFrom > 0 ? criteria.yearFrom : 1950;
        int yearTo = criteria.yearTo > 0 ? criteria.yearTo : currentYear;

        if (!"all".equals(contentType)) active++;
        if (!"popularity.desc".equals(sortBy)) active++;
        if (yearFrom != 1950 || yearTo != currentYear) active++;
        if (criteria.minRating > 0f) active++;
        if (criteria.genreIds != null && !criteria.genreIds.isEmpty()) active++;

        return active;
    }
}
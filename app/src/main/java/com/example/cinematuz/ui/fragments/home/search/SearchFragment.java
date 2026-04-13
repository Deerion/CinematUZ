package com.example.cinematuz.ui.fragments.home.search;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
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
import com.example.cinematuz.utils.LocaleHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private SearchResultAdapter adapter;

    private EditText etSearch;
    private ProgressBar progressBar;
    private RecyclerView rvSearchResults;
    private ImageButton btnClearSearch;
    private ImageButton btnSearchBack;
    private TextView tvSearchEmpty;

    private MaterialButton btnFilterAll, btnFilterMovies, btnFilterTv;
    private SearchResultAdapter.FilterType currentFilter = SearchResultAdapter.FilterType.ALL;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        etSearch = view.findViewById(R.id.etSearch);
        progressBar = view.findViewById(R.id.searchProgressBar);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        btnSearchBack = view.findViewById(R.id.btnSearchBack);
        tvSearchEmpty = view.findViewById(R.id.tvSearchEmpty);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterMovies = view.findViewById(R.id.btnFilterMovies);
        btnFilterTv = view.findViewById(R.id.btnFilterTv);

        setupRecyclerView();
        setupListeners();
        setupObservers();

        setFilter(currentFilter);
        showInitialEmptyState();
        etSearch.requestFocus();
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
        btnClearSearch.setOnClickListener(v -> etSearch.setText(""));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                String lang = LocaleHelper.getLanguage(requireContext()).equals("pl") ? "pl-PL" : "en-US";
                viewModel.onSearchTextChanged(s.toString(), lang);

                if (s.toString().trim().isEmpty()) {
                    adapter.submitList(null);
                    showInitialEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterAll.setOnClickListener(v -> setFilter(SearchResultAdapter.FilterType.ALL));
        btnFilterMovies.setOnClickListener(v -> setFilter(SearchResultAdapter.FilterType.MOVIE));
        btnFilterTv.setOnClickListener(v -> setFilter(SearchResultAdapter.FilterType.TV));
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
            if (etSearch.getText() == null || etSearch.getText().toString().trim().isEmpty()) {
                showInitialEmptyState();
            } else if (results == null || results.isEmpty()) {
                tvSearchEmpty.setText(R.string.empty_search_results);
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
}
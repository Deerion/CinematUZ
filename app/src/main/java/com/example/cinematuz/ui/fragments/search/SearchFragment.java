package com.example.cinematuz.ui.fragments.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cinematuz.R;
import com.example.cinematuz.utils.LocaleHelper;

public class SearchFragment extends Fragment {

    private SearchViewModel viewModel;
    private EditText etSearch;
    private ProgressBar progressBar;

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

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String lang = LocaleHelper.getLanguage(requireContext()).equals("pl") ? "pl-PL" : "en-US";
                viewModel.onSearchTextChanged(s.toString(), lang);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.searchResults.observe(getViewLifecycleOwner(), results -> {
            // Logika wyświetlania wyników zostanie dodana w kroku z UI (Adapter)
        });
    }
}
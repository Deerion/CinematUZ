package com.example.cinematuz.ui.fragments.library;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinematuz.R;
import com.example.cinematuz.databinding.FragmentLibraryBinding;
import com.example.cinematuz.ui.fragments.home.MovieGridAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.tabs.TabLayout;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private MovieGridAdapter adapter;
    private View rootView;

    private String currentStatus = "to_watch";
    private String currentType = "all";

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTypeButtonsUi();
        loadDataFromDatabase();
    }

    private void setupRecyclerView() {
        adapter = new MovieGridAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("MEDIA_ITEM", item);
            Navigation.findNavController(requireView()).navigate(R.id.detailsFragment, bundle);
        });

        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.rvLibrary.setHasFixedSize(true);
        binding.rvLibrary.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvLibrary.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.tabLayoutStatus.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatus = (tab.getPosition() == 0) ? "to_watch" : "watched";
                loadDataFromDatabase();
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

    private void changeTypeFilter(String type) {
        if (currentType.equals(type)) return;
        currentType = type;
        updateTypeButtonsUi();
        loadDataFromDatabase();
    }

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

    private void loadDataFromDatabase() {
        // TODO: Pobierz listę z bazy
        boolean isEmpty = true; // Dla testu pustego stanu

        if (isEmpty) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
        rootView = null;
    }
}
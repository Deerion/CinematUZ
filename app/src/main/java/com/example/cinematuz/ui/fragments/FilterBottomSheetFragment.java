package com.example.cinematuz.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinematuz.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.util.List;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private ChipGroup chipGroupSort;
    private MaterialButtonToggleGroup toggleContentType;
    private ChipGroup chipGroupGenre;
    private RangeSlider sliderYear;
    private Slider sliderRating;
    private TextView tvYearValue;
    private TextView tvRatingValue;
    private MaterialButton btnApply;
    private MaterialButton btnReset;

    private int currentResultsCount = 124;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
        updateApplyButton();
    }

    private void initViews(View view) {
        chipGroupSort = view.findViewById(R.id.chip_group_sort);
        toggleContentType = view.findViewById(R.id.toggle_content_type);
        chipGroupGenre = view.findViewById(R.id.chip_group_genre);
        sliderYear = view.findViewById(R.id.slider_year);
        sliderRating = view.findViewById(R.id.slider_rating);
        tvYearValue = view.findViewById(R.id.tv_year_value);
        tvRatingValue = view.findViewById(R.id.tv_rating_value);
        btnApply = view.findViewById(R.id.btn_apply);
        btnReset = view.findViewById(R.id.btn_reset);

        toggleContentType.check(R.id.btn_type_movie);

        // Zapewnienie, że po kliknięciu na Chip z gatunkiem, pojawia się ikonka X
        for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenre.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                chip.setCloseIconVisible(isChecked);
                if(isChecked) chip.setCloseIconResource(R.drawable.ic_close);
            });
        }
    }

    private void setupListeners() {
        btnReset.setOnClickListener(v -> resetFilters());

        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> updateApplyButton());

        toggleContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateApplyButton();
        });

        sliderYear.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            tvYearValue.setText(values.get(0).intValue() + " - " + values.get(1).intValue());
        });

        sliderRating.addOnChangeListener((slider, value, fromUser) -> {
            tvRatingValue.setText(String.format("%s+", value));
        });

        btnApply.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filtry zastosowane", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void resetFilters() {
        sliderYear.setValues(1950f, 2024f);
        sliderRating.setValue(0.0f);
        toggleContentType.check(R.id.btn_type_movie);
        chipGroupGenre.clearCheck();

        if (chipGroupSort.getChildCount() > 0) {
            ((Chip) chipGroupSort.getChildAt(0)).setChecked(true);
        }
    }

    private void updateApplyButton() {
        // Podmienia 124 na dynamiczną zmienną (będzie pobierana z View modelu)
        String applyText = getString(R.string.filter_apply, currentResultsCount);
        btnApply.setText(applyText);
    }
}
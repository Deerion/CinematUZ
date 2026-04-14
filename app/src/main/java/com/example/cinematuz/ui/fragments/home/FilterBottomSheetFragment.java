package com.example.cinematuz.ui.fragments.home;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.FilterCriteria;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Collections;
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

    // KROK 1: Modulacja wysunięcia modalu (nie na 100% od razu)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetInternal);
                behavior.setSkipCollapsed(false);
                // Modal otworzy się wypełniając ok. 65% ekranu. Można go pociągnąć wyżej.
                behavior.setHalfExpandedRatio(0.65f);
                behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            }
        });
        return dialog;
    }

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

        sliderYear.setValues(1950f, 2024f);
        sliderRating.setValue(0.0f);
        toggleContentType.check(R.id.btn_type_movie);

        setupRealTmdbGenres();
    }

    // KROK 2 i 3: Kolorowanie na wzór HTML Tailwind oraz sortowanie!
    private void setupRealTmdbGenres() {
        chipGroupGenre.removeAllViews();

        List<Pair<Integer, String>> localizedGenres = new ArrayList<>();
        localizedGenres.add(new Pair<>(28, getString(R.string.genre_action)));
        localizedGenres.add(new Pair<>(12, getString(R.string.genre_adventure)));
        localizedGenres.add(new Pair<>(16, getString(R.string.genre_animation)));
        localizedGenres.add(new Pair<>(35, getString(R.string.genre_comedy)));
        localizedGenres.add(new Pair<>(80, getString(R.string.genre_crime)));
        localizedGenres.add(new Pair<>(99, getString(R.string.genre_documentary)));
        localizedGenres.add(new Pair<>(18, getString(R.string.genre_drama)));
        localizedGenres.add(new Pair<>(10751, getString(R.string.genre_family)));
        localizedGenres.add(new Pair<>(14, getString(R.string.genre_fantasy)));
        localizedGenres.add(new Pair<>(36, getString(R.string.genre_history)));
        localizedGenres.add(new Pair<>(27, getString(R.string.genre_horror)));
        localizedGenres.add(new Pair<>(10402, getString(R.string.genre_music)));
        localizedGenres.add(new Pair<>(9648, getString(R.string.genre_mystery)));
        localizedGenres.add(new Pair<>(10749, getString(R.string.genre_romance)));
        localizedGenres.add(new Pair<>(878, getString(R.string.genre_scifi)));
        localizedGenres.add(new Pair<>(53, getString(R.string.genre_thriller)));
        localizedGenres.add(new Pair<>(10752, getString(R.string.genre_war)));
        localizedGenres.add(new Pair<>(37, getString(R.string.genre_western)));

        // Wstępne sortowanie alfabetyczne
        Collections.sort(localizedGenres, (g1, g2) -> g1.second.compareToIgnoreCase(g2.second));

        int colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, 0);
        int colorOnSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, 0);

        for (Pair<Integer, String> genre : localizedGenres) {
            Chip chip = new Chip(requireContext());
            chip.setText(genre.second);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(genre.first);

            // Mechanizm zmiany kolorów (zgodny z makietą Tailwind) i sortowania
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                chip.setCloseIconVisible(isChecked);
                if (isChecked) {
                    chip.setCloseIconResource(R.drawable.ic_close);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 51))); // 20% alpha
                    chip.setTextColor(colorPrimary);
                    chip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 102))); // 40% alpha
                    chip.setChipStrokeWidth(2f);
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 12))); // 5% alpha
                    chip.setTextColor(ColorUtils.setAlphaComponent(colorOnSurface, 180));
                    chip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 25))); // 10% alpha
                    chip.setChipStrokeWidth(1f);
                }

                // Opóźnione sortowanie (pozwala na płynne wykonanie animacji kliknięcia w Chip)
                chipGroupGenre.post(this::sortGenreChips);
            });

            // Wymuszenie załadowania początkowych kolorów (niezaznaczonych)
            chip.setChecked(false);

            chipGroupGenre.addView(chip);
        }
    }

    // Metoda układająca wybrane chipy na początku, resztę alfabetycznie z tyłu
    private void sortGenreChips() {
        List<Chip> chips = new ArrayList<>();
        for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
            chips.add((Chip) chipGroupGenre.getChildAt(i));
        }

        Collections.sort(chips, (c1, c2) -> {
            // Jeśli c1 zaznaczony, a c2 nie -> c1 idzie na początek (zwraca -1)
            if (c1.isChecked() && !c2.isChecked()) return -1;
            // Jeśli c1 nie zaznaczony, a c2 tak -> c2 idzie na początek (zwraca 1)
            if (!c1.isChecked() && c2.isChecked()) return 1;
            // Obydwa zaznaczone LUB obydwa odznaczone -> sortujemy alfabetycznie
            return c1.getText().toString().compareToIgnoreCase(c2.getText().toString());
        });

        chipGroupGenre.removeAllViews();
        for (Chip chip : chips) {
            chipGroupGenre.addView(chip);
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
            FilterCriteria criteria = new FilterCriteria();

            Chip selectedSort = chipGroupSort.findViewById(chipGroupSort.getCheckedChipId());
            if (selectedSort != null) {
                String sortText = selectedSort.getText().toString().toLowerCase();
                if (sortText.contains("popular")) criteria.sortBy = "popularity.desc";
                else if (sortText.contains("dat") || sortText.contains("release")) criteria.sortBy = "primary_release_date.desc";
                else criteria.sortBy = "vote_average.desc";
            } else {
                criteria.sortBy = "popularity.desc";
            }

            criteria.contentType = toggleContentType.getCheckedButtonId() == R.id.btn_type_tv ? "tv" : "movie";

            List<Integer> selectedGenres = new ArrayList<>();
            for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupGenre.getChildAt(i);
                if (chip.isChecked() && chip.getTag() != null) {
                    selectedGenres.add((Integer) chip.getTag());
                }
            }
            criteria.genreIds = selectedGenres;

            criteria.yearFrom = sliderYear.getValues().get(0).intValue();
            criteria.yearTo = sliderYear.getValues().get(1).intValue();
            criteria.minRating = sliderRating.getValue();

            Bundle result = new Bundle();
            result.putSerializable("filter_data", criteria);
            getParentFragmentManager().setFragmentResult("filter_request", result);

            dismiss();
        });
    }

    private void resetFilters() {
        sliderYear.setValues(1950f, 2024f);
        sliderRating.setValue(0.0f);
        toggleContentType.check(R.id.btn_type_movie);

        // Zamiast clearCheck(), aby wywołać listenery i przemalować/posortować od nowa
        for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
            ((Chip) chipGroupGenre.getChildAt(i)).setChecked(false);
        }

        if (chipGroupSort.getChildCount() > 0) {
            ((Chip) chipGroupSort.getChildAt(0)).setChecked(true);
        }
    }

    private void updateApplyButton() {
        btnApply.setText(R.string.filter_apply_simple);
    }
}
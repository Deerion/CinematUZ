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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public static final String ARG_INITIAL_FILTER = "initial_filter_data";

    private ChipGroup chipGroupSort;
    private MaterialButtonToggleGroup toggleContentType;
    private ChipGroup chipGroupGenre;
    private RangeSlider sliderYear;
    private Slider sliderRating;
    private TextView tvYearValue;
    private TextView tvRatingValue;
    private MaterialButton btnApply;
    private MaterialButton btnReset;

    private int colorPrimary;
    private int colorOnPrimary;
    private int colorOnSurface;
    private FilterCriteria initialCriteria;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialCriteria = (FilterCriteria) getArguments().getSerializable(ARG_INITIAL_FILTER);
        }
    }

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

        colorPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, 0);
        colorOnPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, 0);
        colorOnSurface = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSurface, 0);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        sliderYear.setValueTo(currentYear);
        sliderYear.setValues(1950f, (float) currentYear);
        sliderRating.setValue(0.0f);
        toggleContentType.check(R.id.btn_type_all);

        tvYearValue.setText(String.format(Locale.getDefault(), "%d - %d", 1950, currentYear));
        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f+", 0.0f));

        initSortChipsStyle();
        refreshContentTypeButtonsStyle();
        setupRealTmdbGenres(getSelectedContentType());
        applyInitialCriteria(currentYear);
    }

    private void applyInitialCriteria(int currentYear) {
        if (initialCriteria == null) return;

        if ("tv".equals(initialCriteria.contentType)) {
            toggleContentType.check(R.id.btn_type_tv);
        } else if ("movie".equals(initialCriteria.contentType)) {
            toggleContentType.check(R.id.btn_type_movie);
        } else {
            toggleContentType.check(R.id.btn_type_all);
        }

        int checkedSortId = R.id.chip_sort_popularity;
        if ("vote_average.desc".equals(initialCriteria.sortBy)) {
            checkedSortId = R.id.chip_sort_rating;
        } else if ("primary_release_date.desc".equals(initialCriteria.sortBy)
                || "first_air_date.desc".equals(initialCriteria.sortBy)) {
            checkedSortId = R.id.chip_sort_date;
        }
        chipGroupSort.check(checkedSortId);

        int yearFrom = Math.max(1950, initialCriteria.yearFrom > 0 ? initialCriteria.yearFrom : 1950);
        int yearTo = initialCriteria.yearTo > 0 ? Math.min(currentYear, initialCriteria.yearTo) : currentYear;
        if (yearFrom > yearTo) {
            int temp = yearFrom;
            yearFrom = yearTo;
            yearTo = temp;
        }
        sliderYear.setValues((float) yearFrom, (float) yearTo);
        sliderRating.setValue(Math.max(0f, Math.min(10f, initialCriteria.minRating)));

        setupRealTmdbGenres(getSelectedContentType(), initialCriteria.genreIds);
        refreshSortChipsStyle();
        refreshContentTypeButtonsStyle();
        tvYearValue.setText(String.format(Locale.getDefault(), "%d - %d", yearFrom, yearTo));
        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f+", sliderRating.getValue()));
    }

    private void initSortChipsStyle() {
        for (int i = 0; i < chipGroupSort.getChildCount(); i++) {
            if (chipGroupSort.getChildAt(i) instanceof Chip) {
                ((Chip) chipGroupSort.getChildAt(i)).setCheckedIconVisible(false);
            }
        }
        refreshSortChipsStyle();
    }

    private void refreshSortChipsStyle() {
        for (int i = 0; i < chipGroupSort.getChildCount(); i++) {
            if (chipGroupSort.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) chipGroupSort.getChildAt(i);
                boolean checked = chip.isChecked();
                chip.setChipBackgroundColor(ColorStateList.valueOf(checked ? colorPrimary : ColorUtils.setAlphaComponent(colorPrimary, 12)));
                chip.setTextColor(checked ? colorOnPrimary : ColorUtils.setAlphaComponent(colorOnSurface, 180));
                chip.setChipStrokeColor(ColorStateList.valueOf(checked ? colorPrimary : ColorUtils.setAlphaComponent(colorPrimary, 25)));
                chip.setChipStrokeWidth(1f);
            }
        }
    }

    private void refreshContentTypeButtonsStyle() {
        MaterialButton allButton = toggleContentType.findViewById(R.id.btn_type_all);
        MaterialButton movieButton = toggleContentType.findViewById(R.id.btn_type_movie);
        MaterialButton tvButton = toggleContentType.findViewById(R.id.btn_type_tv);
        styleContentTypeButton(allButton, allButton != null && allButton.isChecked());
        styleContentTypeButton(movieButton, movieButton != null && movieButton.isChecked());
        styleContentTypeButton(tvButton, tvButton != null && tvButton.isChecked());
    }

    private void styleContentTypeButton(@Nullable MaterialButton button, boolean checked) {
        if (button == null) return;
        button.setTextColor(checked ? colorOnPrimary : ColorUtils.setAlphaComponent(colorOnSurface, 180));
        button.setBackgroundTintList(ColorStateList.valueOf(checked ? colorPrimary : ColorUtils.setAlphaComponent(colorPrimary, 20)));
        button.setStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, checked ? 0 : 25)));
        button.setStrokeWidth(checked ? 0 : 1);
    }

    private void setupRealTmdbGenres(String contentType) {
        setupRealTmdbGenres(contentType, null);
    }

    private void setupRealTmdbGenres(String contentType, @Nullable List<Integer> selectedGenreIds) {
        chipGroupGenre.removeAllViews();

        List<Pair<Integer, String>> localizedGenres = new ArrayList<>();
        if ("tv".equals(contentType)) {
            localizedGenres.add(new Pair<>(10759, getString(R.string.genre_action_adventure)));
            localizedGenres.add(new Pair<>(16, getString(R.string.genre_animation)));
            localizedGenres.add(new Pair<>(35, getString(R.string.genre_comedy)));
            localizedGenres.add(new Pair<>(80, getString(R.string.genre_crime)));
            localizedGenres.add(new Pair<>(99, getString(R.string.genre_documentary)));
            localizedGenres.add(new Pair<>(18, getString(R.string.genre_drama)));
            localizedGenres.add(new Pair<>(10751, getString(R.string.genre_family)));
            localizedGenres.add(new Pair<>(10762, getString(R.string.genre_kids)));
            localizedGenres.add(new Pair<>(9648, getString(R.string.genre_mystery)));
            localizedGenres.add(new Pair<>(10765, getString(R.string.genre_scifi_fantasy)));
            localizedGenres.add(new Pair<>(10768, getString(R.string.genre_politics)));
            localizedGenres.add(new Pair<>(37, getString(R.string.genre_western)));
        } else if ("movie".equals(contentType)) {
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
        } else {
            addGenreIfMissing(localizedGenres, 28, getString(R.string.genre_action));
            addGenreIfMissing(localizedGenres, 12, getString(R.string.genre_adventure));
            addGenreIfMissing(localizedGenres, 16, getString(R.string.genre_animation));
            addGenreIfMissing(localizedGenres, 35, getString(R.string.genre_comedy));
            addGenreIfMissing(localizedGenres, 80, getString(R.string.genre_crime));
            addGenreIfMissing(localizedGenres, 99, getString(R.string.genre_documentary));
            addGenreIfMissing(localizedGenres, 18, getString(R.string.genre_drama));
            addGenreIfMissing(localizedGenres, 10751, getString(R.string.genre_family));
            addGenreIfMissing(localizedGenres, 14, getString(R.string.genre_fantasy));
            addGenreIfMissing(localizedGenres, 36, getString(R.string.genre_history));
            addGenreIfMissing(localizedGenres, 27, getString(R.string.genre_horror));
            addGenreIfMissing(localizedGenres, 10402, getString(R.string.genre_music));
            addGenreIfMissing(localizedGenres, 9648, getString(R.string.genre_mystery));
            addGenreIfMissing(localizedGenres, 10749, getString(R.string.genre_romance));
            addGenreIfMissing(localizedGenres, 878, getString(R.string.genre_scifi));
            addGenreIfMissing(localizedGenres, 53, getString(R.string.genre_thriller));
            addGenreIfMissing(localizedGenres, 10752, getString(R.string.genre_war));
            addGenreIfMissing(localizedGenres, 37, getString(R.string.genre_western));
            addGenreIfMissing(localizedGenres, 10759, getString(R.string.genre_action_adventure));
            addGenreIfMissing(localizedGenres, 10762, getString(R.string.genre_kids));
            addGenreIfMissing(localizedGenres, 10765, getString(R.string.genre_scifi_fantasy));
            addGenreIfMissing(localizedGenres, 10768, getString(R.string.genre_politics));
        }

        Collections.sort(localizedGenres, (g1, g2) -> g1.second.compareToIgnoreCase(g2.second));

        for (Pair<Integer, String> genre : localizedGenres) {
            Chip chip = new Chip(requireContext());
            chip.setText(genre.second);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTag(genre.first);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                applyGenreChipStyle(chip, isChecked);
                chipGroupGenre.post(this::sortGenreChips);
            });

            chip.setChecked(false);
            applyGenreChipStyle(chip, false);
            if (selectedGenreIds != null && selectedGenreIds.contains(genre.first)) {
                chip.setChecked(true);
            }
            chipGroupGenre.addView(chip);
        }

        sortGenreChips();
    }

    private void addGenreIfMissing(List<Pair<Integer, String>> target, int id, String label) {
        for (Pair<Integer, String> existing : target) {
            if (existing.first == id) return;
        }
        target.add(new Pair<>(id, label));
    }

    private void applyGenreChipStyle(Chip chip, boolean isChecked) {
        chip.setCloseIconVisible(isChecked);
        chip.setCloseIconResource(R.drawable.ic_close);
        chip.setCloseIconTint(ColorStateList.valueOf(isChecked ? colorOnPrimary : colorPrimary));

        if (isChecked) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 51)));
            chip.setTextColor(colorOnPrimary);
            chip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 102)));
            chip.setChipStrokeWidth(2f);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 12)));
            chip.setTextColor(ColorUtils.setAlphaComponent(colorOnSurface, 180));
            chip.setChipStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(colorPrimary, 25)));
            chip.setChipStrokeWidth(1f);
        }
    }

    private void sortGenreChips() {
        List<Chip> chips = new ArrayList<>();
        for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
            chips.add((Chip) chipGroupGenre.getChildAt(i));
        }

        Collections.sort(chips, (c1, c2) -> {
            if (c1.isChecked() && !c2.isChecked()) return -1;
            if (!c1.isChecked() && c2.isChecked()) return 1;
            return c1.getText().toString().compareToIgnoreCase(c2.getText().toString());
        });

        chipGroupGenre.removeAllViews();
        for (Chip chip : chips) {
            chipGroupGenre.addView(chip);
        }
    }

    private String getSelectedContentType() {
        int checkedId = toggleContentType.getCheckedButtonId();
        if (checkedId == R.id.btn_type_tv) return "tv";
        if (checkedId == R.id.btn_type_movie) return "movie";
        return "all";
    }

    private String mapSortBy(int selectedSortId, String contentType) {
        if (selectedSortId == R.id.chip_sort_date) {
            return "tv".equals(contentType) ? "first_air_date.desc" : "primary_release_date.desc";
        }
        if (selectedSortId == R.id.chip_sort_rating) {
            return "vote_average.desc";
        }
        return "popularity.desc";
    }

    private void setupListeners() {
        btnReset.setOnClickListener(v -> resetFilters());

        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            refreshSortChipsStyle();
            updateApplyButton();
        });

        toggleContentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                List<Integer> selectedGenres = getSelectedGenreIds();
                refreshContentTypeButtonsStyle();
                setupRealTmdbGenres(getSelectedContentType(), selectedGenres);
                updateApplyButton();
            }
        });

        sliderYear.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            tvYearValue.setText(String.format(Locale.getDefault(), "%d - %d", values.get(0).intValue(), values.get(1).intValue()));
        });

        sliderRating.addOnChangeListener((slider, value, fromUser) ->
                tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f+", value))
        );

        btnApply.setOnClickListener(v -> {
            FilterCriteria criteria = new FilterCriteria();
            criteria.contentType = getSelectedContentType();
            criteria.sortBy = mapSortBy(chipGroupSort.getCheckedChipId(), criteria.contentType);

            criteria.genreIds = getSelectedGenreIds();

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
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        sliderYear.setValueTo(currentYear);
        sliderYear.setValues(1950f, (float) currentYear);
        sliderRating.setValue(0.0f);
        toggleContentType.check(R.id.btn_type_all);

        setupRealTmdbGenres("all");

        if (chipGroupSort.getChildCount() > 0) {
            ((Chip) chipGroupSort.getChildAt(0)).setChecked(true);
        }

        refreshSortChipsStyle();
        refreshContentTypeButtonsStyle();
        tvYearValue.setText(String.format(Locale.getDefault(), "%d - %d", 1950, currentYear));
        tvRatingValue.setText(String.format(Locale.getDefault(), "%.1f+", 0.0f));
        updateApplyButton();
    }

    private void updateApplyButton() {
        btnApply.setText(R.string.filter_apply_simple);
    }

    private List<Integer> getSelectedGenreIds() {
        List<Integer> selectedGenres = new ArrayList<>();
        for (int i = 0; i < chipGroupGenre.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenre.getChildAt(i);
            if (chip.isChecked() && chip.getTag() != null) {
                selectedGenres.add((Integer) chip.getTag());
            }
        }
        return selectedGenres;
    }
}
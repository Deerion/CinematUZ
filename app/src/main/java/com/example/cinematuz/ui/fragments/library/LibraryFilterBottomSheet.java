package com.example.cinematuz.ui.fragments.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinematuz.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LibraryFilterBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Podepnij layout uproszczonego filtra
        return inflater.inflate(R.layout.bottom_sheet_library_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupSort);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Tutaj w przyszłości przekażesz informację o sortowaniu z powrotem do LibraryFragment
            // Np. przez SharedViewModel lub interfejs.

            // Po wybraniu opcji, zamknij arkusz
            dismiss();
        });
    }
}
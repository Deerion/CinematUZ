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

/**
 * Fragment typu BottomSheet wyświetlający opcje sortowania i filtrowania dla biblioteki filmów użytkownika.
 * Pozwala na zmianę kolejności wyświetlania pozycji (np. po dacie dodania lub ocenie).
 */
public class LibraryFilterBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_library_filter, container, false);
    }

    /**
     * Inicjalizuje widoki i nasłuchiwacz zmian w grupie przełączników (RadioGroup).
     */
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
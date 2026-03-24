package com.example.cinematuz.ui.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cinematuz.R;
import com.example.cinematuz.utils.LocaleHelper;

public class ProfileFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        View languageTile = view.findViewById(R.id.language_settings_tile);

        // listener kliknięć
        languageTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLanguageDialog();
            }
        });

        return view;
    }

    // Metoda do wyświetlania okienka z wyborem języka
    private void showLanguageDialog() {
        final String[] languages = {"Polski", "English"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Wybierz język");
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Wybrano "Polski" (indeks 0)
                    LocaleHelper.setLocale(requireContext(), "pl");
                } else if (which == 1) {
                    // Wybrano "English" (indeks 1)
                    LocaleHelper.setLocale(requireContext(), "en");
                }

                // Odświeżenie aktywności, aby zmiana języka była widoczna od razu
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        });
        builder.show();
    }
}
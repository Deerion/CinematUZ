package com.example.cinematuz.ui.fragments.map;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cinematuz.R;

// Importy dla map Google
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import androidx.lifecycle.ViewModelProvider;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapViewModel mapViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inicjalizacja ViewModelu
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    // ... (tutaj Twoje onViewCreated z getMapAsync) ...

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 1. ODCZYT: Sprawdzamy, czy mamy zapisaną pozycję
        CameraPosition savedPosition = mapViewModel.getSavedCameraPosition();

        if (savedPosition != null) {
            // Jeśli użytkownik już tu był, wracamy w to samo miejsce
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(savedPosition));
        } else {
            // Jeśli to pierwsze uruchomienie, idziemy do Warszawy
            LatLng warszawa = new LatLng(52.2297, 21.0122);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warszawa, 10.0f));
        }

        // Reszta Twojego kodu, np. dodawanie markerów
    }

    @Override
    public void onPause() {
        super.onPause();
        // 2. ZAPIS: Gdy użytkownik opuszcza ekran (np. klika inną zakładkę),
        // zapisujemy gdzie aktualnie patrzył
        if (mMap != null) {
            mapViewModel.setSavedCameraPosition(mMap.getCameraPosition());
        }
    }
}
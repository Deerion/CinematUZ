package com.example.cinematuz.ui.fragments.map;

import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.CameraPosition;

public class MapViewModel extends ViewModel {

    private CameraPosition savedCameraPosition = null;

    public CameraPosition getSavedCameraPosition() {
        return savedCameraPosition;
    }

    public void setSavedCameraPosition(CameraPosition savedCameraPosition) {
        this.savedCameraPosition = savedCameraPosition;
    }
}
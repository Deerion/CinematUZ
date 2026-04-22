package com.example.cinematuz.ui.fragments.map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class MapViewModel extends ViewModel {

    // Prosta klasa wewnętrzna dla kina
    public static class Cinema {
        public String name;
        public String address;
        public LatLng location;

        public Cinema(String name, String address, LatLng location) {
            this.name = name;
            this.address = address;
            this.location = location;
        }
    }

    private final MutableLiveData<List<Cinema>> cinemas = new MutableLiveData<>();

    public MapViewModel() {
        loadCinemas();
    }

    public LiveData<List<Cinema>> getCinemas() {
        return cinemas;
    }

    private void loadCinemas() {
        List<Cinema> list = new ArrayList<>();
        // Dane na podstawie Twojego mockupu HTML
        list.add(new Cinema("Noir Center - Galeria Północ", "ul. Światowida 17, 03-144 Warszawa", new LatLng(52.3117, 20.9678)));
        list.add(new Cinema("Cinema Lux", "Centrum Warszawy", new LatLng(52.2297, 21.0122)));
        list.add(new Cinema("Vortex Screen", "Praga Południe", new LatLng(52.2422, 21.0611)));
        list.add(new Cinema("Cinema City - Focus Mall", "ul. Wrocławska 17, 65-427 Zielona Góra", new LatLng(51.9355, 15.5113)));

        cinemas.setValue(list);
    }
}
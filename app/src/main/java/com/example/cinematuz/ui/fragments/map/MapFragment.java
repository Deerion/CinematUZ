package com.example.cinematuz.ui.fragments.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cinematuz.R;
import com.example.cinematuz.databinding.FragmentMapBinding;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import android.view.inputmethod.EditorInfo;

/**
 * Fragment wyświetlający mapę Google z lokalizacjami kin.
 * Obsługuje geolokalizację użytkownika, wyszukiwanie adresów oraz wyświetlanie detali o wybranych kinach.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Launcher do obsługi próśb o uprawnienia lokalizacyjne.
     */
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if ((fineGranted != null && fineGranted) || (coarseGranted != null && coarseGranted)) {
                    onLocationPermissionGranted();
                } else {
                    binding.permissionOverlay.setVisibility(View.VISIBLE);
                }
            });

    /**
     * Inicjalizuje ViewBinding, ViewModel oraz FusedLocationProviderClient.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        setupUI();
        return binding.getRoot();
    }

    /**
     * Konfiguruje nasłuchiwacze interfejsu użytkownika (przyciski zoomu, lokalizacji, wyszukiwarka).
     */
    private void setupUI() {
        binding.btnMyLocation.setOnClickListener(v -> checkPermissions());
        binding.btnZoomIn.setOnClickListener(v -> { if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomIn()); });
        binding.btnZoomOut.setOnClickListener(v -> { if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.zoomOut()); });

        binding.searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String location = binding.searchEditText.getText().toString();
                if (!location.isEmpty()) searchLocation(location);
                return true;
            }
            return false;
        });
    }

    /**
     * Wyszukuje współrzędne geograficzne na podstawie nazwy miejscowości lub adresu i przesuwa tam kamerę.
     * 
     * @param locationName Nazwa lokalizacji do wyszukania.
     */
    private void searchLocation(String locationName) {
        Geocoder geocoder = new Geocoder(requireContext());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Wywoływane, gdy mapa jest gotowa do użycia. Nakłada styl mapy i inicjuje obserwowanie kin.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        ThemeHelper.applyMapStyle(requireContext(), this.googleMap);

        observeCinemas();
        checkPermissions();

        googleMap.setOnMarkerClickListener(marker -> {
            MapViewModel.Cinema cinema = (MapViewModel.Cinema) marker.getTag();
            if (cinema != null) showCinemaDetails(cinema);
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> binding.cinemaCard.setVisibility(View.GONE));
    }

    /**
     * Wyświetla kartę z informacjami o wybranym kinie.
     */
    private void showCinemaDetails(MapViewModel.Cinema cinema) {
        binding.cinemaTitle.setText(cinema.name);
        binding.cinemaAddress.setText(cinema.address);
        binding.cinemaCard.setVisibility(View.VISIBLE);
    }

    /**
     * Konfiguruje obserwowanie listy kin z ViewModelu i nanosi je na mapę jako markery.
     */
    private void observeCinemas() {
        viewModel.getCinemas().observe(getViewLifecycleOwner(), cinemas -> {
            if (googleMap == null) return;
            googleMap.clear();
            BitmapDescriptor cinemaIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_movie_filter);
            for (MapViewModel.Cinema cinema : cinemas) {
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(cinema.location)
                        .title(cinema.name)
                        .icon(cinemaIcon));
                if (marker != null) marker.setTag(cinema);
            }
        });
    }

    /**
     * Konwertuje wektorową ikonę (drawable) na format BitmapDescriptor używany przez Google Maps.
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Sprawdza uprawnienia lokalizacyjne i prosi o nie, jeśli nie zostały przyznane.
     */
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            onLocationPermissionGranted();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    /**
     * Włącza warstwę "Moja lokalizacja" na mapie i centruje widok na aktualnej pozycji użytkownika.
     */
    @SuppressLint("MissingPermission")
    private void onLocationPermissionGranted() {
        if (googleMap != null) {
            binding.permissionOverlay.setVisibility(View.GONE);
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14f));
                }
            });
        }
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
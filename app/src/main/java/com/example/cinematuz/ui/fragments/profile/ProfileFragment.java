package com.example.cinematuz.ui.fragments.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cinematuz.R;
import com.example.cinematuz.data.models.User;
import com.example.cinematuz.ui.activities.LoginActivity;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;
import java.util.Map;

/**
 * Fragment wyświetlający profil użytkownika.
 * Umożliwia edycję nazwy użytkownika, zmianę awatara, wybór języka aplikacji,
 * przełączanie motywu oraz wylogowanie. Wyświetla również statystyki aktywności.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private GoogleSignInClient mGoogleSignInClient;

    private ImageView profileAvatar;
    private TextView profileName, profileUsername;
    private TextView statMoviesCount, statPoints;

    private FloatingActionButton btnEditAvatar;
    private View editProfileTile;

    private ActivityResultLauncher<String> mGetContent;

    /**
     * Wymagany pusty konstruktor.
     */
    public ProfileFragment() {
    }

    /**
     * Inicjalizuje Firebase, Google SignIn oraz launcher dla wyboru zdjęć z galerii.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        try {
            storage = FirebaseStorage.getInstance();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Storage", e);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    }
                });
    }

    /**
     * Inicjalizuje widoki, ustawia nasłuchiwacze i sprawdza stan zalogowania użytkownika.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        statMoviesCount = view.findViewById(R.id.stat_movies_count);
        statPoints = view.findViewById(R.id.stat_points);

        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        editProfileTile = view.findViewById(R.id.edit_profile_tile);

        View btnLoginGuest = view.findViewById(R.id.btn_login_guest);

        btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        editProfileTile.setOnClickListener(v -> showEditProfileDialog());

        View languageTile = view.findViewById(R.id.language_settings_tile);
        TextView textPl = view.findViewById(R.id.textPl);
        TextView textEn = view.findViewById(R.id.textEn);

        String currentLang = LocaleHelper.getLanguage(requireContext());
        if ("en".equals(currentLang)) {
            setActiveStyle(textEn);
            setInactiveStyle(textPl);
        } else {
            setActiveStyle(textPl);
            setInactiveStyle(textEn);
        }

        textPl.setOnClickListener(v -> changeLanguage("pl"));
        textEn.setOnClickListener(v -> changeLanguage("en"));
        languageTile.setOnClickListener(v -> {
            String nextLang = "pl".equals(LocaleHelper.getLanguage(requireContext())) ? "en" : "pl";
            changeLanguage(nextLang);
        });

        View themeTile = view.findViewById(R.id.theme_settings_tile);
        TextView textThemeLight = view.findViewById(R.id.textThemeLight);
        TextView textThemeDark = view.findViewById(R.id.textThemeDark);

        if (ThemeHelper.isDarkMode(requireContext())) {
            setActiveStyle(textThemeDark);
            setInactiveStyle(textThemeLight);
        } else {
            setActiveStyle(textThemeLight);
            setInactiveStyle(textThemeDark);
        }

        textThemeLight.setOnClickListener(v -> toggleTheme(false));
        textThemeDark.setOnClickListener(v -> toggleTheme(true));
        themeTile.setOnClickListener(v -> toggleTheme(!ThemeHelper.isDarkMode(requireContext())));

        View logoutTile = view.findViewById(R.id.logout_settings_tile);

        if (mAuth.getCurrentUser() != null) {
            logoutTile.setVisibility(View.VISIBLE);
            editProfileTile.setVisibility(View.VISIBLE);
            btnEditAvatar.setVisibility(View.VISIBLE);
            profileUsername.setVisibility(View.VISIBLE);
            btnLoginGuest.setVisibility(View.GONE);

            logoutTile.setOnClickListener(v -> performLogout());
            loadUserProfile();
        } else {
            logoutTile.setVisibility(View.GONE);
            editProfileTile.setVisibility(View.GONE);
            btnEditAvatar.setVisibility(View.GONE);
            profileUsername.setVisibility(View.GONE);

            profileName.setText(R.string.profile_not_logged_in);
            profileAvatar.setImageResource(R.drawable.ic_person);

            btnLoginGuest.setVisibility(View.VISIBLE);

            btnLoginGuest.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });

        }

        return view;
    }

    /**
     * Wyświetla dialog umożliwiający zmianę nazwy użytkownika.
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.profile_edit_title);
        final EditText input = new EditText(requireContext());
        input.setText(profileName.getText().toString());
        builder.setView(input);

        builder.setPositiveButton(R.string.profile_save, (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            }
        });
        builder.setNegativeButton(R.string.profile_cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Aktualizuje nazwę użytkownika w bazie Firestore.
     * 
     * @param newUsername Nowa nazwa użytkownika.
     */
    private void updateUsername(String newUsername) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .update("username", newUsername)
                .addOnSuccessListener(aVoid -> {
                    profileName.setText(newUsername);
                    Toast.makeText(getContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show());
    }

    /**
     * Przesyła wybrany obraz do Firebase Storage jako awatar użytkownika.
     * 
     * @param imageUri URI wybranego obrazu.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || storage == null) return;

        String fileName = "avatars/" + currentUser.getUid() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        Toast.makeText(getContext(), R.string.profile_uploading, Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateAvatarUrl(uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show());
    }

    /**
     * Aktualizuje adres URL awatara w profilu Firestore.
     * 
     * @param downloadUrl Publiczny URL wgranego zdjęcia.
     */
    private void updateAvatarUrl(String downloadUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .update("avatar_url", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(this).load(downloadUrl).circleCrop().into(profileAvatar);
                    Toast.makeText(getContext(), R.string.profile_avatar_updated, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show());
    }

    /**
     * Pobiera dane profilowe zalogowanego użytkownika z Firestore.
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            updateUI(user);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Aktualizuje interfejs użytkownika na podstawie danych z obiektu User.
     * 
     * @param user Obiekt profilu użytkownika.
     */
    private void updateUI(User user) {
        // Korzystamy z modelu User zamiast document.getString(...)
        profileName.setText(user.getUsername() != null ? user.getUsername() : getString(R.string.profile_default_user));
        profileUsername.setText(user.getEmail() != null ? user.getEmail() : getString(R.string.profile_default_email));

        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar_url())
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(profileAvatar);
        } else {
            profileAvatar.setImageResource(R.drawable.ic_person);
        }

        if (user.getStats() != null) {
            int movies = user.getStats().getMoviesWatched();
            int tvShows = user.getStats().getTvShowsWatched();

            statMoviesCount.setText(String.valueOf(movies));
            statPoints.setText(String.valueOf(tvShows));
        }
    }

    /**
     * Zmienia język aplikacji i wymusza odświeżenie aktywności.
     * 
     * @param langCode Kod języka ("pl" lub "en").
     */
    private void changeLanguage(String langCode) {
        if (!langCode.equals(LocaleHelper.getLanguage(requireContext()))) {
            LocaleHelper.setLocale(requireContext(), langCode);
            requireActivity().recreate();
        }
    }

    /**
     * Przełącza motyw aplikacji (jasny/ciemny).
     * 
     * @param dark Prawda dla trybu ciemnego.
     */
    private void toggleTheme(boolean dark) {
        if (dark != ThemeHelper.isDarkMode(requireContext())) {
            ThemeHelper.setDarkMode(requireContext(), dark);
            requireActivity().recreate();
        }
    }

    /**
     * Wylogowuje użytkownika z Firebase i Google, a następnie wraca do ekranu logowania.
     */
    private void performLogout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void setActiveStyle(TextView textView) {
        if (textView != null) {
            textView.setBackgroundResource(R.drawable.bg_switch_active);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void setInactiveStyle(TextView textView) {
        if (textView != null) {
            textView.setBackgroundResource(android.R.color.transparent);
            textView.setTextColor(Color.parseColor("#9E9E9E"));
        }
    }
}
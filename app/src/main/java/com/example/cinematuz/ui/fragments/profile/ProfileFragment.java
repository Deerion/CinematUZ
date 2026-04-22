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
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private GoogleSignInClient mGoogleSignInClient;

    private ImageView profileAvatar;
    private TextView profileName, profileUsername;
    private TextView statMoviesCount, statPoints;
    private TextView rankTitle, rankSubtitle;
    private ProgressBar rankProgress;
    private FloatingActionButton btnEditAvatar;
    private View editProfileTile;

    private ActivityResultLauncher<String> mGetContent;

    public ProfileFragment() {
        // Wymagany pusty konstruktor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Inicjalizacja Storage z próbą pobrania domyślnego wiaderka
        try {
            storage = FirebaseStorage.getInstance();
            Log.d(TAG, "Storage bucket: " + storage.getReference().getBucket());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize default Storage. You might need to provide the bucket URL explicitly.", e);
        }

        // Konfiguracja Google Sign-In do pełnego wylogowania
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        // Rejestracja launchera dla wyboru obrazu
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicjalizacja widoków profilu
        profileAvatar = view.findViewById(R.id.profile_avatar);
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        statMoviesCount = view.findViewById(R.id.stat_movies_count);
        statPoints = view.findViewById(R.id.stat_points);
        rankTitle = view.findViewById(R.id.rank_title);
        rankSubtitle = view.findViewById(R.id.rank_subtitle);
        rankProgress = view.findViewById(R.id.rank_progress);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        editProfileTile = view.findViewById(R.id.edit_profile_tile);
        btnEditAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        editProfileTile.setOnClickListener(v -> showEditProfileDialog());

        // --- OBSŁUGA JĘZYKA ---
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

        // --- OBSŁUGA MOTYWU ---
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

        // --- OBSŁUGA WYLOGOWANIA ---
        View logoutTile = view.findViewById(R.id.logout_settings_tile);

        if (mAuth.getCurrentUser() != null) {
            logoutTile.setVisibility(View.VISIBLE);
            logoutTile.setOnClickListener(v -> performLogout());
            loadUserProfile();
        } else {
            logoutTile.setVisibility(View.GONE);
        }

        return view;
    }

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

    private void updateUsername(String newUsername) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .update("username", newUsername)
                .addOnSuccessListener(aVoid -> {
                    profileName.setText(newUsername);
                    Toast.makeText(getContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating username", e);
                    Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        if (storage == null) {
            Toast.makeText(getContext(), "Storage not initialized correctly", Toast.LENGTH_LONG).show();
            return;
        }

        String fileName = "avatars/" + currentUser.getUid() + "_" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(fileName);

        Toast.makeText(getContext(), R.string.profile_uploading, Toast.LENGTH_SHORT).show();

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateAvatarUrl(downloadUrl);
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed. Bucket name: " + ref.getBucket(), e);
                    Toast.makeText(getContext(), getString(R.string.profile_update_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAvatarUrl(String downloadUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .update("avatar_url", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(this)
                            .load(downloadUrl)
                            .circleCrop()
                            .into(profileAvatar);
                    Toast.makeText(getContext(), R.string.profile_avatar_updated, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating avatar URL", e);
                    Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("profiles").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUI(documentSnapshot);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user data", e);
                    Toast.makeText(getContext(), R.string.profile_update_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(DocumentSnapshot document) {
        String username = document.getString("username");
        String avatarUrl = document.getString("avatar_url");
        String email = document.getString("email");

        profileName.setText(username != null ? username : "User");
        profileUsername.setText(email != null ? email : "No email");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(profileAvatar);
        } else {
            profileAvatar.setImageResource(R.drawable.ic_person);
        }

        Map<String, Object> stats = (Map<String, Object>) document.get("stats");
        if (stats != null) {
            long moviesWatched = stats.get("moviesWatched") != null ? (long) stats.get("moviesWatched") : 0;
            long points = stats.get("points") != null ? (long) stats.get("points") : 0;

            statMoviesCount.setText(String.valueOf(moviesWatched));
            statPoints.setText(String.valueOf(points));

            updateRankInfo(points);
        }
    }

    private void updateRankInfo(long points) {
        // Tymczasowe rangi
        if (points < 100) {
            rankTitle.setText(R.string.profile_rank_newcomer);
            rankSubtitle.setText(String.format(Locale.getDefault(), getString(R.string.profile_xp_to_next_rank), points, 100));
            rankProgress.setProgress((int) points);
        } else if (points < 500) {
            rankTitle.setText(R.string.profile_rank_cinephile);
            rankSubtitle.setText(String.format(Locale.getDefault(), getString(R.string.profile_xp_to_next_rank), points, 500));
            rankProgress.setProgress((int) (points / 5));
        } else {
            rankTitle.setText(R.string.profile_rank_cinephile_elite);
            rankSubtitle.setText(R.string.profile_max_rank);
            rankProgress.setProgress(100);
        }
    }

    private void changeLanguage(String langCode) {
        if (!langCode.equals(LocaleHelper.getLanguage(requireContext()))) {
            LocaleHelper.setLocale(requireContext(), langCode);
            requireActivity().recreate();
        }
    }

    private void toggleTheme(boolean dark) {
        if (dark != ThemeHelper.isDarkMode(requireContext())) {
            ThemeHelper.setDarkMode(requireContext(), dark);
            requireActivity().recreate();
        }
    }

    private void performLogout() {
        // 1. Wylogowanie z Firebase
        mAuth.signOut();

        // 2. Wylogowanie z Google (czyści zapamiętane konto)
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(requireContext(), R.string.settings_logout, Toast.LENGTH_SHORT).show();

            // 3. Przejście do LoginActivity z wyczyszczeniem stosu aktywności
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Zamykamy MainActivity
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
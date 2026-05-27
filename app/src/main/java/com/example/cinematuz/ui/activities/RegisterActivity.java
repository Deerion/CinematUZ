package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinematuz.R;
import com.example.cinematuz.data.models.User;
import com.example.cinematuz.utils.CaptchaStateManager;
import com.example.cinematuz.utils.LocaleHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcaptcha.sdk.HCaptcha;

import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Aktywność rejestracji nowego użytkownika.
 * Obsługuje tworzenie konta w Firebase Auth, tworzenie profilu w Firestore
 * oraz weryfikację hCaptcha.
 */
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String HCAPTCHA_SITE_KEY = "7ed4b1a6-92a6-4082-b4f0-5daa071e8440";

    private Button btnRegister;
    private MaterialCardView cvCaptchaContainer;
    private CheckBox cbCaptcha;
    private CaptchaStateManager captchaStateManager;

    /**
     * Inicjalizuje aktywność, widoki oraz obsługę zdarzeń.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> navigateToLogin());
        }

        ScrollView scrollView = findViewById(R.id.registerScrollView);
        if (scrollView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);
                return WindowInsetsCompat.CONSUMED;
            });
        }
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        btnRegister = findViewById(R.id.btnRegister);
        TextInputEditText etName = findViewById(R.id.etRegisterName);
        TextInputEditText etEmail = findViewById(R.id.etRegisterEmail);
        TextInputEditText etPassword = findViewById(R.id.etRegisterPassword);
        cvCaptchaContainer = findViewById(R.id.cvCaptchaContainer);
        cbCaptcha = findViewById(R.id.cbCaptcha);
        captchaStateManager = new CaptchaStateManager(cvCaptchaContainer, cbCaptcha, btnRegister);

        if (cvCaptchaContainer != null) {
            cvCaptchaContainer.setOnClickListener(v -> {
                HCaptcha.getClient(RegisterActivity.this).verifyWithHCaptcha(HCAPTCHA_SITE_KEY)
                        .addOnSuccessListener(response -> {
                            captchaStateManager.onCaptchaVerified(response.getTokenResult());
                        })
                        .addOnFailureListener(e -> {
                            captchaStateManager.onCaptchaReset();
                            Toast.makeText(RegisterActivity.this, "Błąd hCaptcha", Toast.LENGTH_SHORT).show();
                        });
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Wypełnij pola!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!captchaStateManager.hasVerifiedCaptcha()) {
                    Toast.makeText(RegisterActivity.this, "Potwierdź, że nie jesteś robotem!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tokenToVerify = captchaStateManager.getCaptchaToken();
                captchaStateManager.onSubmitStarted();

                verifyCaptchaAndRegister(tokenToVerify, name, email, password);
            });
        }
    }

    /**
     * Resetuje stan weryfikacji hCaptcha.
     */
    private void resetCaptchaState() {
        if (captchaStateManager != null) {
            captchaStateManager.onCaptchaReset();
        }
    }

    /**
     * Weryfikuje token hCaptcha poprzez zewnętrzny serwer.
     * 
     * @param token Token z hCaptcha SDK.
     * @param name Nazwa użytkownika.
     * @param email Adres e-mail.
     * @param password Hasło.
     */
    private void verifyCaptchaAndRegister(String token, String name, String email, String password) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try { json.put("token", token); } catch (Exception e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://verifycaptcha-lbmgq5tbhq-uc.a.run.app")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    resetCaptchaState();
                    Toast.makeText(RegisterActivity.this, "Błąd sieci", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> performFirebaseRegistration(name, email, password));
                } else {
                    runOnUiThread(() -> {
                        resetCaptchaState();
                        Toast.makeText(RegisterActivity.this, "Weryfikacja nieudana. Spróbuj ponownie.", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Tworzy konto użytkownika w Firebase Auth i zapisuje dane profilowe w Firestore.
     * 
     * @param name Nazwa użytkownika.
     * @param email Adres e-mail.
     * @param password Hasło.
     */
    private void performFirebaseRegistration(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            User newUser = new User(name, email);

                            db.collection("profiles").document(firebaseUser.getUid())
                                    .set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Zarejestrowano pomyślnie!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        resetCaptchaState();
                                        Toast.makeText(RegisterActivity.this, "Błąd zapisu danych: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        if (captchaStateManager != null) {
                            captchaStateManager.onSubmitFinished();
                        }
                        Toast.makeText(RegisterActivity.this, "Błąd: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Dołącza kontekst z obsługą języka.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    /**
     * Przechodzi do ekranu logowania.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
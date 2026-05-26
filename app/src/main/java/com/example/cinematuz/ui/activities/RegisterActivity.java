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

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String HCAPTCHA_SITE_KEY = "7ed4b1a6-92a6-4082-b4f0-5daa071e8440";

    private Button btnRegister;
    private MaterialCardView cvCaptchaContainer;
    private CheckBox cbCaptcha;
    private CaptchaStateManager captchaStateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // W onCreate, po setContentView
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
                            Toast.makeText(RegisterActivity.this, getString(R.string.toast_data_save_error, e.getMessage()), Toast.LENGTH_LONG).show();
                        });
            });
        }

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!captchaStateManager.hasVerifiedCaptcha()) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.toast_confirm_robot), Toast.LENGTH_SHORT).show();
                    return;
                }

                String tokenToVerify = captchaStateManager.getCaptchaToken();
                captchaStateManager.onSubmitStarted();

                verifyCaptchaAndRegister(tokenToVerify, name, email, password);
            });
        }
    }

    private void resetCaptchaState() {
        if (captchaStateManager != null) {
            captchaStateManager.onCaptchaReset();
        }
    }

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
                    captchaStateManager.onCaptchaReset();
                    Toast.makeText(RegisterActivity.this, getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> performFirebaseRegistration(name, email, password));
                } else {
                    runOnUiThread(() -> {
                        captchaStateManager.onCaptchaReset();
                        Toast.makeText(RegisterActivity.this, getString(R.string.toast_verification_failed), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void performFirebaseRegistration(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Profesjonalny zapis obiektu User zamiast mapy
                            User newUser = new User(name, email);

                            db.collection("profiles").document(firebaseUser.getUid())
                                    .set(newUser) // Firestore sam rozpozna strukturę obiektu
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        resetCaptchaState();
                                        Toast.makeText(RegisterActivity.this, getString(R.string.toast_general_error, task.getException().getMessage()), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        // Wewnątrz bloku else po task.isSuccessful()
                        if (captchaStateManager != null) {
                            captchaStateManager.onSubmitFinished();
                        }

// Pobieramy wyjątek, jeśli istnieje, aby przekazać jego treść do stringa
                        String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";

// Podmieniamy na getString z parametrem
                        Toast.makeText(RegisterActivity.this, getString(R.string.toast_general_error, errorMessage), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        // Użyj flag, aby wyczyścić stos aktywności i uniknąć zapętlenia
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Zamknij RegisterActivity
    }
}
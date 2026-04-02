package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinematuz.R;
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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String hCaptchaToken = null;
    private final String HCAPTCHA_SITE_KEY = "7ed4b1a6-92a6-4082-b4f0-5daa071e8440";

    private MaterialCardView cvCaptchaContainer;
    private CheckBox cbCaptcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        mAuth = FirebaseAuth.getInstance();

        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextInputEditText etEmail = findViewById(R.id.etLoginEmail);
        TextInputEditText etPassword = findViewById(R.id.etLoginPassword);

        cvCaptchaContainer = findViewById(R.id.cvCaptchaContainer);
        cbCaptcha = findViewById(R.id.cbCaptcha);

        if (cvCaptchaContainer != null) {
            cvCaptchaContainer.setOnClickListener(v -> {
                HCaptcha.getClient(LoginActivity.this).verifyWithHCaptcha(HCAPTCHA_SITE_KEY)
                        .addOnSuccessListener(response -> {
                            hCaptchaToken = response.getTokenResult();
                            if (cbCaptcha != null) cbCaptcha.setChecked(true);
                            cvCaptchaContainer.setClickable(false);
                            Toast.makeText(LoginActivity.this, "Weryfikacja udana!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            hCaptchaToken = null;
                            if (cbCaptcha != null) cbCaptcha.setChecked(false);
                            Toast.makeText(LoginActivity.this, "Błąd hCaptcha, Sprawdź połączenie z internetem", Toast.LENGTH_SHORT).show();
                        });
            });
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        }

        if (tvSignUpLink != null) {
            tvSignUpLink.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Wypełnij pola!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hCaptchaToken == null) {
                    Toast.makeText(LoginActivity.this, "Potwierdź, że nie jesteś robotem!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // KOPIOWANIE I RESETOWANIE TOKENA - chroni przed already-seen-response
                String tokenToVerify = hCaptchaToken;
                hCaptchaToken = null;
                if (cbCaptcha != null) cbCaptcha.setChecked(false);
                if (cvCaptchaContainer != null) cvCaptchaContainer.setClickable(true);

                verifyCaptchaAndLogin(tokenToVerify, email, password);
            });
        }
    }

    private void verifyCaptchaAndLogin(String token, String email, String password) {
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
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Błąd sieci: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> performFirebaseLogin(email, password));
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Weryfikacja nieudana. Spróbuj ponownie.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void performFirebaseLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        String errorMessage;
                        try {
                            // Rzucamy wyjątek, aby sprawdzić jego typ
                            if (task.getException() != null) {
                                throw task.getException();
                            } else {
                                errorMessage = "Wystąpił nieoczekiwany błąd.";
                            }
                        } catch (com.google.firebase.auth.FirebaseAuthInvalidUserException |
                                 com.google.firebase.auth.FirebaseAuthInvalidCredentialsException e) {
                            // Wspólny komunikat dla błędnego maila LUB hasła
                            errorMessage = "Nieprawidłowy adres e-mail lub hasło.";
                        } catch (com.google.firebase.FirebaseNetworkException e) {
                            errorMessage = "Brak połączenia z internetem.";
                        } catch (Exception e) {
                            // Każdy inny błąd (np. zablokowane konto)
                            errorMessage = "Błąd logowania. Spróbuj ponownie później.";
                        }

                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
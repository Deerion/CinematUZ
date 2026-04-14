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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinematuz.R;
import com.example.cinematuz.utils.CaptchaStateManager;
import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private final String HCAPTCHA_SITE_KEY = "7ed4b1a6-92a6-4082-b4f0-5daa071e8440";

    private Button btnLogin;
    private MaterialCardView cvCaptchaContainer;
    private CheckBox cbCaptcha;
    private CaptchaStateManager captchaStateManager;

    @Override
    protected void onStart() {
        super.onStart();
        // Sprawdzamy sesję ZANIM użytkownik zacznie coś wpisywać
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            goToMainActivity();
        }
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Ustawienia Insets (Edge-to-Edge)
        View mainView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        // --- KONFIGURACJA GOOGLE SIGN-IN ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    }
                }
        );

        // --- INICJALIZACJA WIDOKÓW ---
        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);
        btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogle = findViewById(R.id.btnGoogle);
        TextInputEditText etEmail = findViewById(R.id.etLoginEmail);
        TextInputEditText etPassword = findViewById(R.id.etLoginPassword);

        cvCaptchaContainer = findViewById(R.id.cvCaptchaContainer);
        cbCaptcha = findViewById(R.id.cbCaptcha);
        captchaStateManager = new CaptchaStateManager(cvCaptchaContainer, cbCaptcha, btnLogin);

        // --- LISTENERY ---
        if (cvCaptchaContainer != null) {
            cvCaptchaContainer.setOnClickListener(v -> {
                HCaptcha.getClient(LoginActivity.this).verifyWithHCaptcha(HCAPTCHA_SITE_KEY)
                        .addOnSuccessListener(response -> {
                            captchaStateManager.onCaptchaVerified(response.getTokenResult());
                            Toast.makeText(LoginActivity.this, "Weryfikacja udana!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            captchaStateManager.onCaptchaReset();
                            Toast.makeText(LoginActivity.this, "Błąd hCaptcha", Toast.LENGTH_SHORT).show();
                        });
            });
        }

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> signInWithGoogle());
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Wypełnij pola!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!captchaStateManager.hasVerifiedCaptcha()) {
                    Toast.makeText(LoginActivity.this, "Potwierdź, że nie jesteś robotem!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String tokenToVerify = captchaStateManager.getCaptchaToken();
                captchaStateManager.onSubmitStarted();

                verifyCaptchaAndLogin(tokenToVerify, email, password);
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
    }

    private void resetCaptchaState() {
        if (captchaStateManager != null) {
            captchaStateManager.onCaptchaReset();
        }
    }

    // --- METODY GOOGLE SIGN-IN ---
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Błąd Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, "Błąd autoryzacji Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- METODY LOGOWANIA EMAIL + CAPTCHA ---
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
                runOnUiThread(() -> {
                    resetCaptchaState();
                    Toast.makeText(LoginActivity.this, "Błąd sieci: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> performFirebaseLogin(email, password));
                } else {
                    runOnUiThread(() -> {
                        resetCaptchaState();
                        Toast.makeText(LoginActivity.this, "Weryfikacja hCaptcha nieudana.", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void performFirebaseLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        goToMainActivity();
                    } else {
                        if (captchaStateManager != null) {
                            captchaStateManager.onSubmitFinished();
                        }
                        String errorMessage = "Nieprawidłowy e-mail lub hasło.";
                        if (task.getException() instanceof com.google.firebase.FirebaseNetworkException) {
                            errorMessage = "Brak połączenia z internetem.";
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
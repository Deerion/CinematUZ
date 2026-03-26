package com.example.cinematuz.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinematuz.utils.LocaleHelper;
import com.example.cinematuz.utils.ThemeHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cinematuz.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

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

        // Znajdowanie elementów na podstawie ID
        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSkipLogin = findViewById(R.id.btnSkipLogin);

        // Pola, do których przed chwilą dodaliśmy ID w XML
        TextInputEditText etEmail = findViewById(R.id.etLoginEmail);
        TextInputEditText etPassword = findViewById(R.id.etLoginPassword);

        // Zamknięcie ekranu
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        }

        // Pominięcie logowania
        if (btnSkipLogin != null) {
            btnSkipLogin.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            });
        }

        // Przejście do rejestracji
        if (tvSignUpLink != null) {
            tvSignUpLink.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }

        // Logika Firebase
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                // Zabezpieczenie przed crashem, gdyby pola nadal nie miały ID
                if (etEmail == null || etPassword == null) return;

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Wypełnij wszystkie pola!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Błąd: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }
    }

    // Ta metoda upewnia się, że Activity użyje języka zapisanego w LocaleHelper
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
}
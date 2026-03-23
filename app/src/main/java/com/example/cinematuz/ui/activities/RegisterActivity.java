package com.example.cinematuz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.cinematuz.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Mówimy systemowi: "Sami obsłużymy klawiaturę i paski, nie wtrącaj się"
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_register);

        ScrollView scrollView = findViewById(R.id.registerScrollView);

        // 2. Dodajemy ScrollView "margines" na dole.
        // Jeśli nie ma klawiatury -> margines wielkości dolnego paska.
        // Jeśli jest klawiatura -> margines wielkości klawiatury.
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding);

            return WindowInsetsCompat.CONSUMED;
        });

        mAuth = FirebaseAuth.getInstance();

        // Znajdowanie elementów na podstawie ID z XML
        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        Button btnRegister = findViewById(R.id.btnRegister);

        TextInputEditText etName = findViewById(R.id.etRegisterName);
        TextInputEditText etEmail = findViewById(R.id.etRegisterEmail);
        TextInputEditText etPassword = findViewById(R.id.etRegisterPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);

        // 3. Wymuszamy najazd ekranu po kliknięciu.
        View.OnFocusChangeListener focusListener = (view, hasFocus) -> {
            if (hasFocus) {
                // Opóźnienie 300ms daje klawiaturze czas na wysunięcie się
                scrollView.postDelayed(() -> {
                    scrollView.smoothScrollTo(0, view.getBottom() + 150);
                }, 300);
            }
        };

        // Podpinamy nasłuchiwanie pod oba hasła
        if (etPassword != null) etPassword.setOnFocusChangeListener(focusListener);
        if (etConfirmPassword != null) etConfirmPassword.setOnFocusChangeListener(focusListener);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            });
        }

        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                overridePendingTransition(0, 0);
                finish();
            });
        }

        // Logika Rejestracji i Walidacji
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                if (etEmail == null || etPassword == null || etConfirmPassword == null || etName == null) return;

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();
                String name = etName.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Błędny format email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Hasła nie są identyczne", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Hasło musi mieć min. 6 znaków", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> profile = new HashMap<>();
                                    profile.put("username", name);
                                    profile.put("avatar_url", "");
                                    profile.put("email", email);

                                    db.collection("profiles").document(user.getUid())
                                            .set(profile)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(RegisterActivity.this, "Zarejestrowano pomyślnie!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(RegisterActivity.this, "Konto powstało, ale błąd bazy: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "Błąd rejestracji: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            });
        }
    }
}
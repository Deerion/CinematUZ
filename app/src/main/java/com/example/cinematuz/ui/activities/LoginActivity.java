package com.example.cinematuz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinematuz.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // 1. Zamknięcie logowania (przejście jako "gość" na główny ekran)
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Niszczymy ekran logowania
            });
        }

        // 2. Przejście do rejestracji
        if (tvSignUpLink != null) {
            tvSignUpLink.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                // Wyłączamy animację przejścia, aby zmiana wyglądała jak podmienienie widoku
                overridePendingTransition(0, 0);
                finish(); // Niszczymy ekran logowania, aby nie śmiecić w stosie nawigacji
            });
        }
    }
}
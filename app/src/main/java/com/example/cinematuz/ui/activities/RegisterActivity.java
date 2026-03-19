package com.example.cinematuz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinematuz.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton btnClose = findViewById(R.id.btnClose);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink); // Upewnij się, że to ID zgadza się z Twoim activity_register.xml

        // 1. Zamknięcie rejestracji (przejście jako "gość" na główny ekran)
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Niszczymy ekran rejestracji
            });
        }

        // 2. Powrót do logowania (masz już konto?)
        if (tvLoginLink != null) {
            tvLoginLink.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                // Wyłączamy animację przejścia
                overridePendingTransition(0, 0);
                finish(); // Niszczymy ekran rejestracji
            });
        }
    }
}
package com.example.cinematuz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cinematuz.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Znajdujemy link do rejestracji
        TextView tvSignUpLink = findViewById(R.id.tvSignUpLink);

        // Ustawiamy "słuchacza" kliknięć
        tvSignUpLink.setOnClickListener(v -> {
            // Tworzymy Intencję przejścia do ekranu rejestracji
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
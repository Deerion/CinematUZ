package com.example.cinematuz.ui.activities;

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

        // Powrót do logowania (Przycisk X)
        ImageButton btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        // Powrót do logowania (Link na dole)
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);
        tvLoginLink.setOnClickListener(v -> finish());
    }
}
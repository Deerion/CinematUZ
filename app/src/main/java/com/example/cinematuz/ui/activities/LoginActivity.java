package com.example.cinematuz.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cinematuz.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wyświetlamy tylko nasz nowy ekran (UI), bez żadnej logiki
        setContentView(R.layout.activity_login);
    }
}
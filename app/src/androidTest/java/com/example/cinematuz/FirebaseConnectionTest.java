package com.example.cinematuz;

import static org.junit.Assert.assertNotNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.auth.FirebaseAuth;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FirebaseConnectionTest {

    @Test
    public void testFirebaseInstanceIsNotNull() {
        // Sprawdzamy, czy aplikacja potrafi pobrać instancję Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Jeśli auth nie jest nullem, znaczy że plik JSON i biblioteki działają
        assertNotNull("Połączenie z Firebase nie zostało zainicjalizowane!", auth);
    }
}
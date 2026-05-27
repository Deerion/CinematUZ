package com.example.cinematuz.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class LoginValidatorTest {

    @Test
    public void testEmailValidation() {
        // 1. Oczekujemy SUKCESU (Poprawne maile)
        assertTrue(LoginValidator.isValidEmail("karol@example.com"));
        assertTrue(LoginValidator.isValidEmail("admin.cinematuz@uz.zgora.pl"));

        // 2. Oczekujemy PORAŻKI (Brak znaku @)
        assertFalse("Powinno odrzucić mail bez @", LoginValidator.isValidEmail("karolexample.com"));

        // 3. Oczekujemy PORAŻKI (Brak kropki)
        assertFalse("Powinno odrzucić mail bez kropki", LoginValidator.isValidEmail("karol@example"));

        // 4. Oczekujemy PORAŻKI (Puste pole lub null - zabezpieczenie przed crashem)
        assertFalse("Powinno odrzucić puste pole", LoginValidator.isValidEmail(""));
        assertFalse("Powinno odrzucić null", LoginValidator.isValidEmail(null));
    }

    @Test
    public void testPasswordValidation() {
        // 1. Oczekujemy SUKCESU (Hasło 6 znaków lub więcej)
        assertTrue(LoginValidator.isValidPassword("123456"));
        assertTrue(LoginValidator.isValidPassword("TrudneHaslo!@#"));

        // 2. Oczekujemy PORAŻKI (Za krótkie hasło)
        assertFalse("Powinno odrzucić hasło 5-znakowe", LoginValidator.isValidPassword("12345"));

        // 3. Oczekujemy PORAŻKI (Puste pole lub null)
        assertFalse("Powinno odrzucić puste hasło", LoginValidator.isValidPassword("   ")); // same spacje
        assertFalse("Powinno odrzucić null", LoginValidator.isValidPassword(null));
    }
}
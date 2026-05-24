package com.example.cinematuz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.cinematuz.ui.activities.LoginActivity;
import com.example.cinematuz.utils.LocaleHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LocaleTest {

    @Test
    public void testLanguageChange_UpdatesUI() {
        // 1. Uruchom Activity
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {

            // 2. Ustaw język na angielski i odśwież kontekst (używając Twojego LocaleHelper)
            scenario.onActivity(activity -> {
                LocaleHelper.setLocale(activity, "en");
                activity.recreate(); // Wymuszenie odświeżenia widoków
            });

            // 3. Sprawdź, czy tekst zmienił się na angielski (przykład: przycisk logowania)
            // Zakładając, że w strings.xml masz "Login" dla EN
            onView(withId(R.id.btnLogin)).check(matches(withText("Login")));

            // 4. Ustaw język na polski
            scenario.onActivity(activity -> {
                LocaleHelper.setLocale(activity, "pl");
                activity.recreate();
            });

            // 5. Sprawdź, czy tekst zmienił się na polski
            onView(withId(R.id.btnLogin)).check(matches(withText("Zaloguj")));
        }
    }
}
package com.example.cinematuz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.cinematuz.ui.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testLoginFlow() {
        // 1. Wpisz e-mail
        onView(withId(R.id.etLoginEmail))
                .perform(typeText("Test@example.com"), closeSoftKeyboard());

        // 2. Wpisz hasło
        onView(withId(R.id.etLoginPassword))
                .perform(typeText("Test12345"), closeSoftKeyboard());

        // 3. Kliknij przycisk logowania
        // Używamy allOf, aby Espresso wiedziało, że chodzi o widoczny przycisk
        onView(allOf(withId(R.id.btnLogin), isDisplayed()))
                .perform(click());
    }
}
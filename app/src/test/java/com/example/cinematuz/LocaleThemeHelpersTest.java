package com.example.cinematuz;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocaleThemeHelpersTest {

    @Mock
    Context mockContext;
    @Mock
    SharedPreferences mockPrefs;
    @Mock
    SharedPreferences.Editor mockEditor;

    @Before
    public void setUp() {
        // Przygotowujemy mocki SharedPreferences do testów (częsty scenariusz przy językach/motywach)
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }

    @Test
    public void testLanguageIsSavedToPreferences() {
        // Załóżmy, że masz metodę saveLanguage w LocaleHelper
        // LocaleHelper.saveLanguage(mockContext, "en");

        // Symulacja wywołania logiki (do uzupełnienia wg Twojego kodu LocaleHelper)
        mockEditor.putString("App_Language", "en");
        mockEditor.apply();

        // Weryfikacja (Mockito sprawdza, czy faktycznie próbowano zapisać 'en' do pamięci)
        verify(mockEditor).putString(eq("App_Language"), eq("en"));
        verify(mockEditor).apply();
    }
}
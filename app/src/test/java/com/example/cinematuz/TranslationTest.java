package com.example.cinematuz;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.assertTrue;

public class TranslationTest {

    @Test
    public void testAllStringsAreTranslatedToEnglish() throws Exception {
        // Podajemy ścieżki do obu plików z tekstami
        File defaultStringsFile = new File("src/main/res/values/strings.xml"); // Główny (Polski)
        File englishStringsFile = new File("src/main/res/values-en/strings.xml"); // Angielski

        // Upewniamy się, że test w ogóle znalazł te pliki
        assertTrue("Nie znaleziono głównego pliku strings.xml. Sprawdź ścieżkę.", defaultStringsFile.exists());
        assertTrue("Nie znaleziono pliku values-en/strings.xml. Sprawdź ścieżkę.", englishStringsFile.exists());

        // Wyciągamy wszystkie klucze z obu plików
        Set<String> defaultKeys = extractStringKeys(defaultStringsFile);
        Set<String> englishKeys = extractStringKeys(englishStringsFile);

        // Tworzymy zbiór brakujących tłumaczeń (Klucze polskie, których NIE MA w angielskich)
        Set<String> missingTranslations = new HashSet<>(defaultKeys);
        missingTranslations.removeAll(englishKeys);

        // Budujemy czytelny komunikat błędu, jeśli coś znajdziemy
        StringBuilder errorMessage = new StringBuilder("ZAPOMNIAŁEŚ O TYCH TŁUMACZENIACH W values-en/strings.xml:\n");
        for (String key : missingTranslations) {
            errorMessage.append("- ").append(key).append("\n");
        }

        // Test kończy się sukcesem TYLKO WTEDY, gdy lista braków jest pusta
        assertTrue(errorMessage.toString(), missingTranslations.isEmpty());
    }

    /**
     * Pomocnicza metoda, która otwiera plik XML i szuka w nim tagów <string name="nazwa_klucza">
     */
    private Set<String> extractStringKeys(File xmlFile) throws Exception {
        Set<String> keys = new HashSet<>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("string");

        for (int i = 0; i < nList.getLength(); i++) {
            Element element = (Element) nList.item(i);

            // Ignorujemy specjalne teksty (np. klucze API), które mają atrybut translatable="false"
            String translatable = element.getAttribute("translatable");
            if (!"false".equals(translatable)) {
                keys.add(element.getAttribute("name"));
            }
        }
        return keys;
    }
}
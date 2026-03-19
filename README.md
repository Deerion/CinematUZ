# CinematUZ 🎬
**Społecznościowa platforma do śledzenia produkcji filmowych i wspólnego planowania seansów.**

Projekt realizowany w ramach przedmiotu: *Programowanie urządzeń mobilnych*.

## 🛠️ Zespół Deweloperski
<div align="center">

| Project Lead<br>& Developer | Android<br>Developer | Android<br>Developer | Scrum Master, UX/UI Designer<br>& Developer |
| :---: | :---: | :---: |:---:|
| <img src="https://github.com/Deerion.png" width="100" height="100"> | <img src="https://github.com/Karolkzsp5.png" width="100" height="100"> | <img src="https://github.com/piolud.png" width="100" height="100"> | <img src="https://github.com/lifeoverthinker.png" width="100" height="100"> |
| **Hubert Jarosz** | **Karol Kondracki** | **Piotr Ludowicz** | **Martyna Niżyńska** |
| [@Deerion](https://github.com/Deerion) | [@Karolkzsp5](https://github.com/Karolkzsp5) | [@piolud](https://github.com/piolud) | [@lifeoverthinker](https://github.com/lifeoverthinker) |

</div>

## 🚀 Kluczowe funkcjonalności (Wymagania Sylabusa)
Aplikacja została zaprojektowana zgodnie z wytycznymi programowymi przedmiotu:

* 🔐 **Autoryzacja i Bezpieczeństwo:** Rejestracja i logowanie (Firebase Authentication) z walidacją **Captcha**.
* 🌍 **Lokalizacja i Języki:** Pełne wsparcie dla **dwóch wersji językowych** oraz obsługa **motywów** (Light/Dark Mode).
* 👥 **Interakcje Społecznościowe:**
    * Tworzenie grup lokalnych przy użyciu **Bluetooth**.
    * System zaproszeń do znajomych oparty na **kodach QR** (skanowanie aparatem).
* 🗺️ **Mapa Kin:** Moduł Google Maps z funkcją geolokalizacji i wskazywania najbliższych placówek.
* 🔔 **Powiadomienia:** System powiadomień Push o zdarzeniach w grupie (dołączenie/usunięcie użytkownika).
* 🎲 **Akcelerometr:** Funkcja losowania filmu ("Maszyna losująca") poprzez potrząśnięcie urządzeniem.
* 📊 **Widget Statystyk:** Systemowy widget wyświetlający podsumowanie aktywności na ekranie głównym.
* ☁️ **Baza Danych:** Synchronizacja profilu (User info) z bazą **Firebase** oraz lokalna baza danych Room.
* 🔍 **Wyszukiwarka:** Integracja z zewnętrznym API TMDB.
* 📄 **Raporty:** Eksport rankingów filmowych do formatu PDF.

## 🛠️ Technologie i narzędzia
* **IDE:** Android Studio
* **Język:** Java
* **Backend & Cloud:** Firebase (Auth, Database, Cloud Messaging)
* **Baza lokalna:** Room Persistence Library
* **Sensory i Hardware:** Akcelerometr, Bluetooth Adapter, Camera (QR Reader)
* **Zewnętrzne API:** Google Maps SDK, TMDB API
* **VCS:** GIT

## 📄 Licencja
Projekt udostępniany na licencji **MIT**.

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

// 1. ODCZYT PLIKU local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
// Pobieramy klucz. Jeśli go nie ma, używamy pustego stringa, aby uniknąć błędów kompilacji
val tmdbApiKey = localProperties.getProperty("TMDB_API_KEY") ?: ""

android {
    namespace = "com.example.cinematuz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cinematuz"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. PRZEKAZANIE KLUCZA DO KLASY BuildConfig
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        // 3. WŁĄCZENIE GENEROWANIA KLASY BuildConfig (wymagane w nowszych wersjach Android Studio)
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Testy
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Glide (do ładowania obrazków)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Shimmer (skeleton loading)
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.github.hcaptcha:hcaptcha-android-sdk:3.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
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
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.junit)
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
    implementation("com.google.firebase:firebase-storage")

    // Lokalna baza Room
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

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
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.lifecycle:lifecycle-process:2.6.2")

    // QR Code
    implementation(libs.zxing.android.embedded)
    implementation(libs.zxing.core)
    implementation("com.squareup:seismic:1.0.3")
    implementation("com.google.android.gms:play-services-nearby:19.0.0")

    // --- TESTY JEDNOSTKOWE (Folder: src/test/java) ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.10.0")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")

    // Opcjonalnie: ułatwia mockowanie klas finalnych i statycznych w Mockito
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // --- TESTY INTERFEJSU UI (Folder: src/androidTest/java) ---
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Dodatkowe reguły i narzędzia dla testów Androida (np. ActivityScenarioRule)
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    testImplementation("org.robolectric:robolectric:4.11.1")
}
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // REEMPLAZADO: ksp es la opción moderna y más rápida para el Room Compiler
    id("com.google.devtools.ksp") // Se asume que este ID está definido en tu settings.gradle o build.gradle a nivel proyecto
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "bw.development.nutriscan"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "bw.development.nutriscan"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Compose & Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // BOM (Bill of Materials) - Usada solo una vez para gestionar versiones
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- Room Database ---
    // 1. KTX (runtime y corrutinas)
    implementation(libs.androidx.room.ktx)
    implementation(libs.play.services.code.scanner)
    // 2. KSP (Kotlin Symbol Processing, reemplaza a KAPT)
    ksp(libs.androidx.room.compiler)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation("androidx.compose.material:material-icons-extended")

// --- Networking ---
    implementation(libs.retrofit.core)
    implementation(libs.okhttp) // <-- AÑADIR ESTA LÍNEA
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    // --- Barcode Scanner ---
    implementation(libs.mlkit.barcode.scanning)

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
}

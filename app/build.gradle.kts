plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.2.20"
}

android {
    namespace = "com.ace.wallpaperrex"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ace.wallpaperrex"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // optional if using BOM
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Compose BOM (manages Compose versions)
    val composeBom = platform("androidx.compose:compose-bom:2025.09.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(kotlin("reflect"))
    implementation(libs.kotlinx.serialization.json)

    // Compose UI libraries
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material.icons.extended.android) // Use the latest stable version


    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

//    // Optional integrations
    implementation(libs.androidx.activity.compose)
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
//    implementation("androidx.compose.runtime:runtime-livedata")
//    implementation("androidx.compose.runtime:runtime-rxjava2")
    implementation(libs.androidx.adaptive)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}

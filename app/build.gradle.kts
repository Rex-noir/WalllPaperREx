plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "2.2.20"
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.androidx.room)
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

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.accompanist.placeholder.material)

    //Coil
    implementation(libs.coil)
    implementation(libs.coil.kt.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.network.okhttp)
    implementation(libs.lib.zoomable)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

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

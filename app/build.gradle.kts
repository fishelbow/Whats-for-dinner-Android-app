plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
//    id("com.google.devtools.ksp") version "2.1.0-1.0.29"



}

android {
    namespace = "com.example.possiblythelastnewproject" // this is another not sure

    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.PossiblyTheLastNewProject" // change this for android studio project name
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        kotlinCompilerExtensionVersion = "1.6.10"
    }
}

dependencies {


    implementation(libs.kotlinx.serialization.json.v160)

    implementation(libs.androidx.compose.foundation.foundation)

    implementation(libs.kotlin.reflect)

    implementation(libs.foundation)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.animation.core)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.kotlin.metadata.jvm)

    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.flow.layout)

    // Hilt Core
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.material3)
    kapt(libs.hilt.android.compiler)

    // Hilt + Jetpack ViewModel
    implementation(libs.androidx.hilt.navigation.compose)
   // kapt(libs.androidx.hilt.compiler)
  //  ksp(libs.hilt.android.compiler)
   // ksp(libs.androidx.hilt.compiler)

    // Hilt + Lifecycle + ViewModel
    implementation(libs.androidx.room.runtime)
    kapt("androidx.room:room-compiler:2.7.2") // ← this goes here
   // ksp("androidx.room:room-compiler:2.7.2")


    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Google ML Kit Barcode Scanner
    implementation(libs.barcode.scanning)

    // Google ML Kit Text Recognition V2 (using the bundled offline model)
    implementation(libs.text.recognition)

    implementation(libs.animation)

    // Core KTX for Navigation
    implementation(libs.androidx.navigation.runtime.ktx)

    // coil for images
    implementation(libs.coil.compose)

    // room for db
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.room.runtime.android)
    debugImplementation(libs.androidx.core)

    // Material 3 core widgets
    implementation(libs.androidx.compose.material3.material3)

    // Extended icon pack (adds CameraAlt, Checklist, Kitchen, …)
    implementation(libs.androidx.material.icons.extended)

    //
    implementation(libs.guava)

    //
    implementation(libs.listenablefuture)

    //
    implementation(libs.androidx.material3)

    // Core Compose dependencies.
    implementation(libs.ui) // includes pointer input (consume) and theming.
    implementation(libs.androidx.foundation) // includes layout modifiers such as offset.
    implementation(libs.material3)
    implementation(libs.androidx.animation) // for advanced animations (optional)

    // Navigation Compose (if needed)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compiler)
    implementation(libs.material)
    implementation(libs.androidx.monitor)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.runtime.livedata)

    //Leaky Canary for memory leaks
    debugImplementation(libs.leakcanary.android)

    // Tooling for previews.
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Kotlin Coroutines for async animations/gestures.
    implementation(libs.kotlinx.coroutines.android)

    // Other AndroidX dependencies.
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(kotlin("reflect"))
}
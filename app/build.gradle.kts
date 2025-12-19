plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.hullor.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hullor.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 5
        versionName = "1.1"
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
        viewBinding = true
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx.v1120)

    implementation(libs.glide)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.media3.exoplayer.v111)
    implementation(libs.media3.exoplayer.v111)

    annotationProcessor(libs.compiler)
    implementation (libs.okhttp)

    // Firebase BoM
    implementation(platform(libs.firebase.bom.v3230))

    // Firebase modules (no version needed)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.com.google.firebase.firebase.auth.ktx)
    implementation(libs.com.google.firebase.firebase.firestore.ktx)
    implementation(libs.androidx.appcompat.v170)

    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.swiperefreshlayout.v110) // ✅ stable version

    // Lifecycle + Navigation
    implementation(libs.androidx.lifecycle.livedata.ktx.v286)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v286)
    implementation(libs.androidx.navigation.fragment.ktx.v283)
    implementation(libs.androidx.navigation.ui.ktx.v283)

    // ✅ Firebase (using BoM)
    implementation(platform(libs.firebase.bom.v3330))
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)

    // ✅ Glide
    implementation(libs.glide)
    kapt(libs.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)
}

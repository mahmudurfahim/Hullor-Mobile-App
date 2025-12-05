
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // ✅ Make sure this version matches Firebase's latest Gradle plugin
        classpath(libs.google.services.v442)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
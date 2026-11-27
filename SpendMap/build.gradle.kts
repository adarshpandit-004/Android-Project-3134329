// Top-level Gradle configuration shared by the app modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Make the Secrets Gradle Plugin available to the app module.
    // It is applied in app/build.gradle.kts, not at the root level.
    alias(libs.plugins.secrets.gradle) apply false
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // ➡️ ADD THIS LINE for the Google Services Plugin
    id("com.google.gms.google-services") version "4.4.1" apply false
    // Note: Always use the latest version available (4.4.1 is current, but check Firebase docs for updates)
}
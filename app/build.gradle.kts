plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.brooksconnect"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.brooksconnect"
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:2.5.0")

    // Coil (Image Loading)
    implementation("io.coil-kt:coil:2.6.0")

    // OSMDroid
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Google Generative AI - Removed (Using REST API now)
    // implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // OkHttp (for Groq Fallback)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
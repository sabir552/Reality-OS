plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.realityos.realityos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.realityos.realityos"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
// In app/build.gradle.kts

android {
    namespace = "com.realityos.realityos"
    compileSdk = 34

    defaultConfig {
        // ... existing config ...
    }

    // ▼▼▼ ADD THIS ENTIRE BLOCK ▼▼▼
    signingConfigs {
        create("release") {
            // You can also use a file if you prefer
            val keystoreFile = project.rootProject.file("keystore.jks")
            if (keystoreFile.exists()) {
                 storeFile = keystoreFile
                 storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                 keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                 keyPassword = System.getenv("SIGNING_KEY_PRIVATE_PASSWORD")
            }
        }
    }
    // ▲▲▲ END OF BLOCK TO ADD ▲▲▲

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // And add this line to link the signing config
            signingConfig = signingConfigs.getByName("release")
        }
    }
    // ... rest of the file ...
}

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core & UI (with explicit versions)
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
implementation("androidx.activity:activity-compose:1.8.1")
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.ui:ui-graphics:1.5.4")
implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2") // Note this specific version
implementation("androidx.navigation:navigation-compose:2.7.6")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")

// ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// Google Play Billing
implementation("com.android.billingclient:billing-ktx:6.1.0")

// Testing (no changes needed here)
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4") // Also specify version here
debugImplementation("androidx.compose.ui:ui-tooling:1.5.4") // And here
debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4") // And here

}

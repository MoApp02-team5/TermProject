plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.termproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.termproject"
        minSdk = 24
        targetSdk = 34
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val camerax_version = "1.3.0-alpha04"

dependencies {

    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    // If you want to additionally use the CameraX VideoCapture library
    implementation ("androidx.camera:camera-video:${camerax_version}")
    // If you want to additionally use the CameraX View class
    implementation ("androidx.camera:camera-view:${camerax_version}")
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation ("androidx.camera:camera-mlkit-vision:${camerax_version}")
    // If you want to additionally use the CameraX Extensions library
    implementation ("androidx.camera:camera-extensions:${camerax_version}")

    // Compose
    implementation ("androidx.activity:activity-compose:1.7.2")
    implementation ("androidx.compose.runtime:runtime:1.5.0")

    // Activity Result API
    implementation ("androidx.activity:activity:1.7.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
}

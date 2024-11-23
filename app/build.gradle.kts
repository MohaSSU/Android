import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Firebase 설정
}

android {
    namespace = "com.example.mohassu"
    compileSdk = 34

    buildFeatures {
        dataBinding = true
    }

    defaultConfig {
        applicationId = "com.example.mohassu"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 네이버 지도 CLIENT_ID를 로드 (manifestPlaceholders에 전달)
        val localProperties = File(rootDir, "local.properties")
        val clientId = if (localProperties.exists()) {
            val properties = Properties().apply {
                load(localProperties.inputStream())
            }
            properties.getProperty("NAVER_MAPS_CLIENT_ID", "")
        } else ""
        manifestPlaceholders["NAVER_CLIENT_ID"] = clientId
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // Naver Map
    implementation("com.naver.maps:map-sdk:3.16.0")
}
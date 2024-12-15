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
        viewBinding = true
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

    viewBinding {
        enable = true
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

    // splash 화면 추가
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0")){
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    // Firebase Messaging 라이브러리 추가
    implementation("com.google.firebase:firebase-messaging") // 버전 명시 없이 추가
}

    implementation ("com.google.firebase:firebase-firestore:24.7.0")
    implementation ("com.google.firebase:firebase-storage:20.2.0")// 최신 버전 확인

    // Naver Map SDK
    implementation("com.naver.maps:map-sdk:3.19.1")

    // Naver Map 위치추적
    implementation("com.google.android.gms:play-services-location:21.0.1")

    //  Authentication
    implementation("com.google.firebase:firebase-auth:22.0.0")

    //  TimeTableView
    implementation ("com.github.tlaabs:TimetableView:1.0.3-fx1")


    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    //CirecleImageView
    implementation ("de.hdodenhof:circleimageview:2.2.0")


}
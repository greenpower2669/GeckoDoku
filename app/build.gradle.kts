plugins {
    id("com.android.application")
}

android {
    namespace = "com.greenpower2669.geckodoku"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.greenpower2669.geckodoku"
        minSdk = 26
        targetSdk = 36
        versionCode = 5
        versionName = "0.4.1-dev"
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
}

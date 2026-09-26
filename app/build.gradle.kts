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
        versionCode = 10
        versionName = "0.9.0-dev"
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("../assets")
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
}


dependencies {
    testImplementation("junit:junit:4.13.2")
}

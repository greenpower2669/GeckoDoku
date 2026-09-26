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
        versionCode = 18
        versionName = "0.10.7-voice-ab-exp"
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
    implementation(
        "com.github.k2-fsa:sherpa-onnx:v1.13.8"
    )
    testImplementation("junit:junit:4.13.2")
}

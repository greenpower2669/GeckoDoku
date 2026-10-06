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
        versionCode = 79
        versionName = "0.15.44-dev"
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

        create("phone") {
            initWith(
                getByName("release")
            )
            signingConfig =
                signingConfigs
                    .getByName("debug")
            isDebuggable = false
            matchingFallbacks +=
                listOf("release")
        }
    }
}


dependencies {
    implementation(
        files(
            "libs/sherpa-onnx-1.13.8.aar"
        )
    )
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

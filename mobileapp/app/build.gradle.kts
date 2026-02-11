plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mobileapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.mobileapp"
        minSdk = 30
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.glide)
    implementation(libs.google.material)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //noinspection UseTomlInstead
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    //noinspection UseTomlInstead
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    //noinspection UseTomlInstead
    implementation("com.google.code.gson:gson:2.13.2")
    //noinspection UseTomlInstead
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation(libs.core.splashscreen)
    // WebSocket - STOMP
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation(libs.mpandroidchart)
}
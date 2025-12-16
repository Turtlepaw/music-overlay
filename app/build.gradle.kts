import com.android.build.api.dsl.Packaging
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("key.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.turtlepaw.overlay"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.turtlepaw.overlay"
        minSdk = 30
        targetSdk = 35
        versionCode = 12
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/*"
        }
    }
    lint {
        disable += "NullSafeMutableLiveData"
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("io.ktor:ktor-server-core:2.3.4") // Core server functionality
    implementation("io.ktor:ktor-server-netty:2.3.4") // Netty engine
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7") // Latest version
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") // Latest version
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    implementation("androidx.compose.material3:material3")

    implementation("com.google.code.gson:gson:2.11.0")

    implementation(libs.compose.cloudy)

    // Icons
    implementation(libs.material.icons.extended)

    //implementation("com.github.Turtlepaw:nearby_settings:1.0.1-alpha2")
    implementation(files("E:/nearby_settings/tv_core/build/outputs/aar/tv_core-debug.aar"))
    implementation("com.google.android.gms:play-services-nearby:19.3.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.2")

    implementation("io.github.raamcosta.compose-destinations:core:2.1.1")
    ksp("io.github.raamcosta.compose-destinations:ksp:2.1.1")

    // for bottom sheet destination support, also add
    implementation("io.github.raamcosta.compose-destinations:bottom-sheet:2.1.1")

    // https://mvnrepository.com/artifact/androidx.compose.material3/material3
    implementation("androidx.compose.material3:material3-android:1.5.0-alpha08")
}
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
}

fun signingProperty(name: String): String? =
    localProperties.getProperty(name)
        ?: providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name).orNull

val keystoreFileName = signingProperty("KEYSTORE_FILE") ?: "masjidscreen.keystore"
val keystorePassword = signingProperty("KEYSTORE_PASSWORD")
    ?: throw GradleException(
        "KEYSTORE_PASSWORD is missing. Copy local.properties.example to local.properties and set signing values."
    )
val keyAliasName = signingProperty("KEY_ALIAS") ?: "androiddebugkey"
val keyPasswordValue = signingProperty("KEY_PASSWORD") ?: keystorePassword
val releaseKeystore = file(keystoreFileName)
if (!releaseKeystore.isFile) {
    throw GradleException(
        "Signing keystore is missing: ${releaseKeystore.absolutePath}\n" +
            "Place your private .keystore / .jks in app/ (gitignored) and set KEYSTORE_FILE in local.properties."
    )
}
val googleWebClientId = signingProperty("GOOGLE_WEB_CLIENT_ID").orEmpty()

android {
    namespace = "com.mirazanik.masjidscreen"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mirazanik.masjidscreen"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    signingConfigs {
        create("shared") {
            storeFile = releaseKeystore
            storePassword = keystorePassword
            keyAlias = keyAliasName
            keyPassword = keyPasswordValue
        }
    }

    buildTypes {
        debug {
            // Same key on every PC so sideload / OTA can update without uninstall.
            signingConfig = signingConfigs.getByName("shared")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("shared")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.activity.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    // Prayer Times
    implementation(libs.adhan)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // QR pairing
    implementation(libs.zxing.core)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.mlkit.barcode.scanning)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

val devGoogleServices = file("src/dev/google-services.json")
val prodGoogleServices = file("src/prod/google-services.json")
if (!prodGoogleServices.isFile) {
    throw GradleException(
        "Production Firebase config is missing: ${prodGoogleServices.absolutePath}\n" +
            "Download google-services.json from Firebase (mosque-live-screen) and save it as app/src/prod/google-services.json (gitignored)."
    )
}
if (!devGoogleServices.isFile) {
    logger.warn(
        "Dev Firebase config is missing: ${devGoogleServices.absolutePath}\n" +
            "Create the mosque-live-screen-dev Firebase project (see SETUP.md), register " +
            "package com.mirazanik.masjidscreen.dev, and copy google-services.json there.\n" +
            "Prod builds still work. Dev variants are disabled until that file exists."
    )
}

androidComponents {
    beforeVariants { builder ->
        val isDev = builder.productFlavors.any { it.second == "dev" }
        if (isDev && !devGoogleServices.isFile) {
            builder.enable = false
        }
    }
}

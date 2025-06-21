@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.richard.musicplayer"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file("streamtune.keystore")
            storePassword = "streamtune123"
            keyAlias = "streamtune"
            keyPassword = "streamtune123"
        }
    }

    defaultConfig {
        applicationId = "com.richard.musicplayer"
        minSdk = 26
        targetSdk = 36
        versionCode = 53
        versionName = "25.06.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            
            // Otimizações para R8
            isDebuggable = false
            isJniDebuggable = false
            renderscriptOptimLevel = 3
            
            // Configurações de build para economizar memória
            multiDexEnabled = true
        }
        debug {
            applicationIdSuffix = ".debug"
            multiDexEnabled = true
        }

        // userdebug is release builds without minify
        create("userdebug") {
            initWith(getByName("release"))
            isMinifyEnabled = false
            isShrinkResources = false
            multiDexEnabled = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

// build variants and stuff
    splits {
        abi {
            isEnable = true
            reset()

            // all common abis
            // include("x86_64", "x86", "armeabi-v7a", "arm64-v8a") // universal
            isUniversalApk = false
        }
    }

    // Configurações para otimizar uso de memória durante compilação removidas
    // dexOptions foi removido no AGP 8.0 - otimizações são automáticas

    flavorDimensions.add("abi")

    productFlavors {
        // universal
        create("universal") {
            isDefault = true
            dimension = "abi"
            ndk {
                abiFilters.addAll(listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a"))
            }
        }
        // arm64 only
        create("arm64") {
            dimension = "abi"
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }
        // x86_64 only
        create("x86_64") {
            dimension = "abi"
            ndk {
                abiFilters.add("x86_64")
            }
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "SonsPhere-${variant.versionName}-${variant.baseName}.apk"
                output.outputFileName = outputFileName
            }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        jvmTarget = "21"
    }

    tasks.withType<KotlinCompile> {
        exclude("**/*FFMpegScanner.kt")
    }

    // for IzzyOnDroid
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    lint {
        disable += "MissingTranslation"
        disable += "ByteOrderMark"
        disable += "ExtraTranslation"
        abortOnError = false
        baseline = file("lint-baseline.xml")
    }

    androidResources {
        generateLocaleConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.guava)
    implementation(libs.coroutines.guava)
    implementation(libs.concurrent.futures)

    implementation(libs.activity)
    implementation(libs.navigation)
    implementation(libs.hilt.navigation)
    implementation(libs.datastore)

    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.animation)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.icons.extended)

    implementation(libs.adaptive)

    implementation(libs.viewmodel)
    implementation(libs.viewmodel.compose)

    implementation(libs.material3)
    implementation(libs.palette)
    implementation(projects.materialColorUtilities)

    implementation(libs.coil)

    implementation(libs.shimmer)

    implementation(libs.media3)
    implementation(libs.media3.session)
    implementation(libs.media3.okhttp)
    implementation(libs.media3.workmanager)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.apache.lang3)

    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    implementation(projects.innertube)
    implementation(projects.kugou)
    implementation(projects.lrclib)

    coreLibraryDesugaring(libs.desugaring)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.serialization.json)

    // Add kotlinx-serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Core Splashscreen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    /*
    "JitPack builds are broken with the latest CMake version.
    Please download the [aar](https://github.com/Kyant0/taglib/releases) manually but not use maven."
     */
//    implementation(libs.taglib) // jitpack
    implementation(files("../prebuilt/taglib-1.0.2-outertune-universal-release.aar")) // prebuilt
//    implementation("com.kyant:taglib") // custom

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
}
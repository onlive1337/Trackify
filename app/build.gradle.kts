import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val keystoreProperties: Properties? = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.inputStream()
    ?.use { stream -> Properties().apply { load(stream) } }

val hasReleaseKeystore: Boolean = System.getenv("KEYSTORE_FILE") != null || keystoreProperties != null

val sdkDirPath: String? = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")
    ?: rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.inputStream()
        ?.use { stream -> Properties().apply { load(stream) }.getProperty("sdk.dir") }

val hasInstalledNdk: Boolean = sdkDirPath?.let { dir ->
    File(dir, "ndk").listFiles()?.any { it.isDirectory } == true
} == true

android {
    namespace = "com.onlive.trackify"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.onlive.trackify"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    bundle {
        language {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_FILE")?.let { file(it) }
                ?: keystoreProperties?.getProperty("storeFile")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: keystoreProperties?.getProperty("storePassword")
            keyAlias = System.getenv("KEY_ALIAS")
                ?: keystoreProperties?.getProperty("keyAlias")
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: keystoreProperties?.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            if (hasInstalledNdk) {
                ndk {
                    debugSymbolLevel = "FULL"
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseKeystore) {
                signingConfigs.getByName("release")
            } else {
                null
            }
        }

        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-DEBUG"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        if (!hasInstalledNdk) {
            jniLibs {
                keepDebugSymbols += "**/libandroidx.graphics.path.so"
            }
        }
    }
}

gradle.taskGraph.whenReady {
    val releaseArtifactTasks = setOf(
        "assembleRelease",
        "bundleRelease",
        "packageRelease",
        "packageReleaseBundle"
    )
    val buildingRelease = allTasks.any { it.name in releaseArtifactTasks }
    if (buildingRelease && !hasReleaseKeystore) {
        throw GradleException(
            "Release build requested but no signing keystore was found. " +
                "Set the KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS and KEY_PASSWORD " +
                "environment variables, or add a keystore.properties file to the project root. " +
                "The release build will NOT fall back to the debug signing key."
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.work.runtime.ktx)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
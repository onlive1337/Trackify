import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val hasReleaseKeystore: Boolean = System.getenv("KEYSTORE_FILE") != null ||
    rootProject.file("keystore.properties").exists()

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
            storeFile = if (System.getenv("KEYSTORE_FILE") != null) {
                file(System.getenv("KEYSTORE_FILE"))
            } else {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                    keystoreProperties["storeFile"]?.let { file(it) }
                } else {
                    null
                }
            }

            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: run {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                    keystoreProperties["storePassword"] as String?
                } else {
                    null
                }
            }

            keyAlias = System.getenv("KEY_ALIAS") ?: run {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                    keystoreProperties["keyAlias"] as String?
                } else {
                    null
                }
            }

            keyPassword = System.getenv("KEY_PASSWORD") ?: run {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                    keystoreProperties["keyPassword"] as String?
                } else {
                    null
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
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
        jniLibs {
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.material)

    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.gson)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
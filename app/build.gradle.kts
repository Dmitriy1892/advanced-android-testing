plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleKsp)
    alias(libs.plugins.kotlinKapt)
    alias(libs.plugins.androidxSafeargs)
}

android {
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    namespace = "com.example.android.architecture.blueprints.todoapp"

    defaultConfig {
        applicationId = "com.example.android.architecture.blueprints.reactive"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles.addAll(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    file("proguard-rules.pro")
                )
            )
        }
    }

    dataBinding {
        enable = true
        enableForTests = true
    }

    val javaVersion = JavaVersion.toVersion(libs.versions.javaVersion.get())

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    // App dependencies
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.annotations)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    // Architecture Components
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.commonJava8)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Dependencies for local unit tests
    testImplementation(libs.junit)
    testImplementation(libs.hamcrest)

    testImplementation(libs.androidx.archCoreTest)
    testImplementation(libs.androidx.junitKtx)
    testImplementation(libs.androidx.testCore)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlinx.coroutinesTest)

    // AndroidX Test - Instrumented testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espressoCore)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.kotlinx.coroutinesTest)
    androidTestImplementation(libs.androidx.espressoContrib)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.dexmaker.mockito)
    implementation(libs.androidx.fragmentTesting)
    implementation(libs.androidx.testCore)

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.fragment.ktx)
}

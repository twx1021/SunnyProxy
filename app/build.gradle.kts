import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun signingProperty(name: String): String =
    (findProperty(name) as String?)
        ?: localProperties.getProperty(name)
        ?: ""

android {
    namespace = "hev.sockstun"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.xingzhi.forwardtool"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("x86")
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    signingConfigs {
        create("release") {
            val releaseStoreFile = signingProperty("RELEASE_STORE_FILE")
            storeFile = if (releaseStoreFile.isNotBlank()) file(releaseStoreFile) else file("missing-release-keystore.jks")
            storePassword = signingProperty("RELEASE_STORE_PASSWORD")
            keyAlias = signingProperty("RELEASE_KEY_ALIAS")
            keyPassword = signingProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

}


import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val naverMapNcpKeyId = localProperties.getProperty("NAVER_MAP_NCP_KEY_ID")
    ?: localProperties.getProperty("NAVER_MAP_CLIENT_ID")
    ?: ""
val naverSearchClientId = localProperties.getProperty("NAVER_SEARCH_CLIENT_ID")
    ?: localProperties.getProperty("NAVER_LOCAL_SEARCH_CLIENT_ID")
    ?: ""
val naverSearchClientSecret = localProperties.getProperty("NAVER_SEARCH_CLIENT_SECRET")
    ?: localProperties.getProperty("NAVER_LOCAL_SEARCH_CLIENT_SECRET")
    ?: ""

fun buildConfigString(value: String): String {
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escaped\""
}

android {
    namespace = "com.cafepickuporder.android"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.cafepickuporder.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["naverMapNcpKeyId"] = naverMapNcpKeyId
        buildConfigField("String", "NAVER_MAP_NCP_KEY_ID", buildConfigString(naverMapNcpKeyId))
        buildConfigField("String", "NAVER_SEARCH_CLIENT_ID", buildConfigString(naverSearchClientId))
        buildConfigField("String", "NAVER_SEARCH_CLIENT_SECRET", buildConfigString(naverSearchClientSecret))
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.naver.maps:map-sdk:3.23.3")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

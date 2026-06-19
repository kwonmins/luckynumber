import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun String.asBuildConfigString(): String = "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val admobAppId = localProperties.getProperty(
    "admob.app.id",
    "ca-app-pub-3914006572487084~7933303244"
)
val admobRewardedAdUnitId = localProperties.getProperty(
    "admob.rewarded.ad.unit.id",
    "ca-app-pub-3914006572487084/3237515345"
)
val useRealAdsInDebug = localProperties.getProperty("admob.use.real.ads.in.debug", "false").toBoolean()

android {
    namespace = "com.example.unum"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.unum"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            localProperties.getProperty("openai.api.key", "").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "KAKAO_NATIVE_APP_KEY",
            localProperties.getProperty("kakao.native.app.key", "").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_URL",
            localProperties.getProperty("supabase.url", "").trimEnd('/').asBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            localProperties.getProperty("supabase.anon.key", "").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "ADMOB_APP_ID",
            admobAppId.asBuildConfigString()
        )
        buildConfigField(
            "String",
            "ADMOB_REWARDED_AD_UNIT_ID",
            admobRewardedAdUnitId.asBuildConfigString()
        )
        buildConfigField(
            "Boolean",
            "ADMOB_USE_REAL_ADS_IN_DEBUG",
            useRealAdsInDebug.toString()
        )
        manifestPlaceholders["kakaoRedirectScheme"] =
            "kakao${localProperties.getProperty("kakao.native.app.key", "")}"
        manifestPlaceholders["adMobApplicationId"] = admobAppId
    }

    buildTypes {
        debug {
            if (!useRealAdsInDebug) {
                manifestPlaceholders["adMobApplicationId"] = "ca-app-pub-3940256099942544~3347511713"
            }
        }
        release {
            isMinifyEnabled = false
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
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.12.00")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("com.kakao.sdk:v2-user:2.23.4")
    implementation("com.google.android.gms:play-services-ads:25.3.0")

    implementation("com.google.android.material:material:1.12.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

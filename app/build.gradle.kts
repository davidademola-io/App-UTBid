plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")   // 👈 required with Kotlin 2.0+
}


android {
    namespace = "com.example.apputbid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apputbid"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }



    buildTypes {
        debug {
            // Hardcoded admin credentials for testing
            buildConfigField("String", "ADMIN_USERNAME", "\"admin\"")
            buildConfigField("String", "ADMIN_PASSWORD", "\"admin123\"")  // optional if moving from ADMIN_PASSCODE

            // (Your existing passcode)
            buildConfigField("String", "ADMIN_PASSCODE", "\"1234\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Secure, injected from environment variables during CI/CD
            val adminUser = System.getenv("ADMIN_USERNAME") ?: "set_admin_username"
            val adminPass = System.getenv("ADMIN_PASSWORD") ?: "set_admin_password"

            buildConfigField("String", "ADMIN_USERNAME", "\"${adminUser}\"")
            buildConfigField("String", "ADMIN_PASSWORD", "\"${adminPass}\"")
            buildConfigField("String", "ADMIN_PASSCODE", "\"${System.getenv("ADMIN_PASSCODE") ?: "set_me"}\"")
        }
    }


    // 👇 Enable Compose and set the compiler extension
    buildFeatures { compose = true }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.core:core-i18n:1.0.0")
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("com.google.code.gson:gson:2.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}



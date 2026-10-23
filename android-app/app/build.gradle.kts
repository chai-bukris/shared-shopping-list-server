plugins { alias(libs.plugins.android.application) }

android {
    namespace = "com.example.sharedshoppinglist"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.example.sharedshoppinglist"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

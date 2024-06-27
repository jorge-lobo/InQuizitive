plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt") apply true
}

android {
    namespace = "com.example.inquizitive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.inquizitive"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // FastAdapter
    val latestFastAdapterRelease = "5.7.0"
    implementation("com.mikepenz:fastadapter:${latestFastAdapterRelease}")
    implementation("com.mikepenz:fastadapter-extensions-expandable:${latestFastAdapterRelease}")
    implementation("com.mikepenz:fastadapter-extensions-binding:${latestFastAdapterRelease}") // diff util helpers
    implementation("com.mikepenz:fastadapter-extensions-diff:${latestFastAdapterRelease}") // diff util helpers
    implementation("com.mikepenz:fastadapter-extensions-drag:${latestFastAdapterRelease}") // drag support
    implementation("com.mikepenz:fastadapter-extensions-paged:${latestFastAdapterRelease}") // paging support
    implementation("com.mikepenz:fastadapter-extensions-scroll:${latestFastAdapterRelease}") // scroll helpers
    implementation("com.mikepenz:fastadapter-extensions-swipe:${latestFastAdapterRelease}") // swipe support
    implementation("com.mikepenz:fastadapter-extensions-ui:${latestFastAdapterRelease}") // pre-defined ui components
    implementation("com.mikepenz:fastadapter-extensions-utils:${latestFastAdapterRelease}") // needs the `expandable`, `drag` and `scroll` extension.
    implementation("androidx.databinding:databinding-common:8.3.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("com.google.android.material:material:1.5.0")
}
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

}

android {
    namespace = "com.mbelchuke.marketingcloud"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Create configuration for copyDependencies.
configurations {
    create("copyDependencies")
}

dependencies {

    // Add package dependency for binding library
    // Uncomment line below and replace {dependency.name.goes.here} with your dependency
    // implementation("{dependency.name.goes.here}")
    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("org.altbeacon:android-beacon-library:2.20")

    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation ("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation ("androidx.browser:browser:1.8.0")
    implementation ("com.google.firebase:firebase-messaging:24.1.0")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("com.salesforce.marketingcloud:marketingcloudsdk:9.0.0")


    // Copy dependencies for binding library

    "copyDependencies"("androidx.constraintlayout:constraintlayout:2.2.1")
    "copyDependencies"("com.salesforce.marketingcloud:marketingcloudsdk:9.0.0")
}

// Copy dependencies for binding library.
project.afterEvaluate {
    tasks.register<Copy>("copyDeps") {
        from(configurations["copyDependencies"])
        into("${projectDir}/build/outputs/deps")
    }
    tasks.named("preBuild") { finalizedBy("copyDeps") }
}

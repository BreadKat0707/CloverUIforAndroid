plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

val cloverUiVersion: String = project.findProperty("cloverUiVersion")?.toString()
    ?: error("Missing 'cloverUiVersion' in gradle.properties")

version = cloverUiVersion

android {
    namespace = "cn.lemondrop.clover"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 31
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
    buildToolsVersion = "37.0.0"

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    api(libs.haze)
    api(libs.haze.blur)
    implementation(libs.lucide)
    implementation(libs.fluent)

    debugImplementation(libs.androidx.compose.ui.tooling)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "cn.lemondrop.clover"
            artifactId = "clover-ui"
            version = cloverUiVersion
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

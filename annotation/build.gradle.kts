import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.native.cocoapods)
    alias(libs.plugins.android.library)
}

version = rootProject.extra["groupVersion"] as String
group = "com.gargantua7.ksp.sqldelight.adapter"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }

        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()

    sourceSets {
        androidMain {
            dependsOn(commonMain.get())
        }
    }
}

android {
    namespace = "com.gargantua7.kotlin.ksp.sqldelight.annotation"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}
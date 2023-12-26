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
    }
    iosX64()
    iosArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "annotation"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
    }
}

android {
    namespace = "com.gargantua7.kotlin.ksp.sqldelight.annotation"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}
//
//publishing {
//    publications {
//        withType<MavenPublication> {
//        from(components[target.name])
//
//        groupId = "com.gargantua7.ksp.sqldelight.adapter"
//        artifactId = "annotation-${target.name}"
//        version = rootProject.extra["groupVersion"] as String
//        }
//    }
//}
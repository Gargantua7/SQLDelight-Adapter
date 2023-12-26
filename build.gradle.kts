import java.util.Properties

plugins {
    `maven-publish`
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.native.cocoapods) apply false
    alias(libs.plugins.android.library) apply false
}

val groupVersion by extra("0.1.0-SNAPSHOT")
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

group = "com.gargantua7.kotlin.ksp"
version = "1.0-SNAPSHOT"

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.21"))
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gargantua7/kotlin-ksp-sqldelight-adapter")
            credentials {
                username = localProperties.getProperty("github.actor")
                password = localProperties.getProperty("github.token")
            }
        }
    }
}

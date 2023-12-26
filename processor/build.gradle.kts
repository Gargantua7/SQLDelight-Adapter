plugins {
    `maven-publish`
    kotlin("jvm")
}

dependencies {
    implementation(libs.ksp.api)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("processor") {
            groupId = "com.gargantua7.ksp.sqldelight.adapter"
            artifactId = "processor"
            version = rootProject.extra["groupVersion"] as String

            from(components["kotlin"])
        }
    }
}
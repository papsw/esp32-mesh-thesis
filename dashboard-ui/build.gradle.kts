import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.0"
    kotlin("plugin.serialization") version "1.9.22"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    implementation("io.ktor:ktor-client-core-jvm:2.3.8")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.8")
    implementation("io.ktor:ktor-client-websockets-jvm:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm, TargetFormat.Msi)
        }
    }
}
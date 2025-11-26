plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-client-core-jvm:2.3.8")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.8")
}

application {
    mainClass.set("SimulatorMainKt")
}
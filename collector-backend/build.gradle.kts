plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fazecast:jSerialComm:2.9.3")
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("io.ktor:ktor-server-core-jvm:2.3.8")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.8")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.8")
}

application {
    mainClass.set("CollectorKt")
}

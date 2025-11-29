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

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.10")
    testImplementation("io.ktor:ktor-client-websockets-jvm:2.3.10")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:2.3.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

application {
    mainClass.set("collector.CollectorKt")
}

tasks.test {
    useJUnitPlatform()
}
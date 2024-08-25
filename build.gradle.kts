plugins {
    application
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "dev.yuua"

tasks.shadowJar {
    archiveBaseName.set("fortune")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.kordex.dev/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")

    // Discord Related
    implementation("dev.kord:kord-core:0.14.0")
    implementation("dev.kord:kord-core-voice:0.14.0")
    implementation("dev.kordex:kord-extensions:2.2.0-SNAPSHOT")
    implementation("dev.kordex.modules:dev-unsafe:2.2.0-SNAPSHOT")

    // Ktor
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    // Other Libraries
    implementation("org.reflections:reflections:0.10.2")
    implementation("com.github.ajalt.clikt:clikt:4.0.0")
    implementation("com.sksamuel.hoplite:hoplite-core:2.7.5")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.7.5")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.0.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.yuua.fortune.MainKt")
}
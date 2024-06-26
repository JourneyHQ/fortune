plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.8.0"
    application
}

group = "dev.yuua"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://maven.yuua.dev/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Logging
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")

    // Discord Related
    implementation("dev.kord:kord-core:unknown-d-field-fix-SNAPSHOT")
    implementation("dev.kord:kord-core-voice:0.13.1")
    implementation("dev.kord:kord-voice:0.13.1")
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.6.0-SNAPSHOT")
    implementation("com.kotlindiscord.kord.extensions:unsafe:1.6.0-SNAPSHOT")
    implementation("dev.arbjerg:lavaplayer:2.0.2")
    implementation("com.github.aikaterna:lavaplayer-natives:original-SNAPSHOT")

    // Ktor
    implementation("io.ktor:ktor-client-cio-jvm:2.2.4")
    implementation("io.ktor:ktor-client-cio:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-client-core:2.2.4")

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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "dev.yuua.fortune.MainKt"
    }

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    archiveBaseName.set("fortune")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter {
            it.name.endsWith("jar")
        }.map { zipTree(it) }
    })
}
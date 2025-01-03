/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.9/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    alias(libs.plugins.jvm)
    kotlin("plugin.serialization") version "1.9.0"
    application
}

repositories {
    mavenCentral()
}


dependencies {
    implementation("io.ktor:ktor-server-core:2.1.0")
    implementation("io.ktor:ktor-server-netty:2.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.1.0")
    implementation(libs.guava)
    implementation(project(":race"))


    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "org.example.ServerKt"
}

 tasks.named<Test>("test") {
     useJUnitPlatform()
 }

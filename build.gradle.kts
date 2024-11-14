plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("plugin.serialization") version "1.9.25"
}

group = "com.github.hadywalied"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    implementation("org.xerial:sqlite-jdbc:3.44.1.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }  // Add this line for SQLite support
    implementation("io.insert-koin:koin-core:3.5.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.+") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.+") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.2.6")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

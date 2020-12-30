import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.4.20"

    id("com.github.johnrengelman.shadow") version "6.1.0"
    application
    idea
}

group = "net.hugebot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.kohsuke:github-api:1.117")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "net.hugebot.MainKt"
}

tasks {
    named<ShadowJar>("shadowJar") {
        manifest {
            attributes(mapOf("Main-Class" to "net.hugebot.MainKt"))
        }
        archiveBaseName.set("Updater")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}
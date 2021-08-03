plugins {
    kotlin("jvm") version "1.5.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")  // For monun's libraries.
    maven("https://papermc.io/repo/repository/maven-public/") // PaperMC
}

dependencies {
    compileOnly(kotlin("stdlib")) // Kotlin
    implementation("com.github.hazae41:mc-kutils:+")
    compileOnly("io.github.monun:kommand-api:+") // For future command creation convenience.
    compileOnly("io.papermc.paper:paper-api:+") // Paper Latest
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "16" // https://papermc.io/java16 | 모장 개놈들아
    }
    processResources {
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
        filteringCharset = "UTF-8"
    }
    shadowJar {
        archiveClassifier.set("dist")
        archiveVersion.set("")
    }
    create<Copy>("dist") {
        from (shadowJar)
        into(".\\")
    }
}
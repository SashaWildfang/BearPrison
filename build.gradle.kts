plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    // Standard repository for LuckPerms
    maven("https://repo.lucko.me/")

    // Repository for PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // This tells Gradle to look in your 'libs' folder for local jars
    flatDir {
        dirs("libs")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    // Add LuckPerms API
    compileOnly("net.luckperms:api:5.4")

    // Add PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")

    // Include your local BearEconomy library from the /libs folder
    // Ensure the filename in libs matches this exact name
    compileOnly(files("libs/BearEconomy-1.0.jar"))
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        // Set version to 0.9 as requested
        val props = mapOf("version" to "0.9", "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    id("io.github.goooler.shadow") version "8.1.8"
}

allprojects {
    group = "net.azisaba"
    version = "1.3.0"
    description = "LifeNewPvE"

    apply {
        plugin("java-library")
        plugin("maven-publish")
        plugin("io.github.goooler.shadow")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
        implementation("com.zaxxer:HikariCP:6.0.0")
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://mvn.lumine.io/repository/maven-public/")
        maven("https://repo.onarandombox.com/content/groups/public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.azisaba.net/repository/maven-public/")
    }

    tasks {
        withType<JavaCompile> { options.encoding = "UTF-8" }
        withType<Javadoc> { options.encoding = "UTF-8" }
        base.archivesName.set("LifeNewPvE")

        shadowJar {
            relocate("org.jetbrains", "net.azisaba.lifenewpve.lib.org.jetbrains")
            relocate("com.zaxxer.hikari", "net.azisaba.lifenewpve.lib.com.zaxxer")
        }
    }
}

repositories {
    mavenCentral()
}

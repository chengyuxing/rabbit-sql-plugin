plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.github.chengyuxing"
version = "2.4.24"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.github.chengyuxing:rabbit-sql:8.1.4") {
        exclude("org.slf4j", "slf4j-api")
    }
    testImplementation("junit:junit:4.13.2")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2023.1")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("com.intellij.database",
            "com.intellij.java",
            "com.intellij.spring",
            "org.jetbrains.kotlin"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("251.*")
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

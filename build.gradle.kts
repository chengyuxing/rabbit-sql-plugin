plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.github.chengyuxing"
version = "2.4.18-IJ2021.3"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.github.chengyuxing:rabbit-sql:8.1.0") {
        exclude("org.slf4j", "slf4j-api")
    }
    testImplementation("junit:junit:4.13.2")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.3")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("com.intellij.database",
            "com.intellij.java",
            "com.intellij.spring",
            "org.jetbrains.kotlin",
            "org.jetbrains.plugins.yaml"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("221.*")
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

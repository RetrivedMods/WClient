@file:Suppress("VulnerableLibrariesLocal")

plugins {
    id("java-library")
    alias(libs.plugins.checkerframework)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    // Lombok as annotation processor (avoids circular dependency)
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // compileOnly(libs.netty.transport.raknet)
    api(project(":relay:Network:transport-raknet"))
    api(project(":relay:Protocol:bedrock-codec"))
    api(libs.snappy)
}
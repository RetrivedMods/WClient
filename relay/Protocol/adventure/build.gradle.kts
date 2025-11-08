plugins {
    id("java-library")
    alias(libs.plugins.checkerframework)
}

dependencies {
    // Lombok as annotation processor (avoids circular dependency)
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    
    api(project(":relay:Protocol:bedrock-codec"))
    api(libs.adventure.text.serializer.legacy)
    api(libs.adventure.text.serializer.json)

    // Tests
    testImplementation(libs.junit)
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "org.cloudburstmc.protocol.adventure")
    }
}

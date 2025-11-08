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
    
    api(project(":relay:Protocol:common"))
    api(libs.adventure.text.serializer.legacy)
    api(libs.adventure.text.serializer.json)
    api(platform(libs.fastutil.bom))
    api(libs.netty.buffer)
    api(libs.fastutil.long.common)
    api(libs.fastutil.long.obj.maps)
    api(libs.jose4j)
    api(libs.nbt)
    api(libs.jackson.annotations)
    api(libs.jackson.databind)

    // Tests
    testImplementation(libs.junit)
}
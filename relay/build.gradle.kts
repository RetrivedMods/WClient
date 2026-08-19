plugins {
    id("java-library")
    kotlin("jvm")
}

group = "com.retrivedmods.wrelay"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.jose4j)
    implementation(libs.jackson.databind)
    implementation(libs.kotlinx.coroutines.core)
    
    // Use api to expose these to the app module
    api(libs.minecraft.auth)
    api(project(":relay:Network:transport-raknet"))

    // Previously vendored locally under relay/Protocol/* (stuck at an old Beta1 snapshot that
    // only understood protocol versions up to ~898 / Bedrock 1.21.130). Switched to the actual
    // upstream CloudburstMC/Protocol artifacts published on opencollab's snapshot repo (already
    // declared in settings.gradle.kts), which track current Bedrock protocol versions. This is a
    // real version jump (Beta1 -> Beta12) so some packet/codec API usage elsewhere in this project
    // may need small fixes to compile - check the first build's errors.
    api("org.cloudburstmc.protocol:bedrock-codec:3.0.0.Beta12-SNAPSHOT")
    api("org.cloudburstmc.protocol:bedrock-connection:3.0.0.Beta12-SNAPSHOT")
    api("org.cloudburstmc.protocol:common:3.0.0.Beta12-SNAPSHOT")
    api(libs.bundles.netty)
    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

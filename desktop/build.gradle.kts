plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.retrivedmods.wclient"
version = "12"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("com.radiantbyte.novaclient.desktop.DesktopLauncherKt")
}

dependencies {
    implementation(project(":relay"))
    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.core)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
    implementation(libs.kotlinx.serialization.json.jvm)
    implementation(libs.minecraft.auth)
    implementation(libs.jose4j)
    implementation(libs.jackson.databind)
    
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.shadowJar {
    archiveBaseName.set("NovaClient-Desktop")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())
    
    manifest {
        attributes(
            "Main-Class" to "com.radiantbyte.novaclient.desktop.DesktopLauncherKt"
        )
    }
    
    mergeServiceFiles()
    
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

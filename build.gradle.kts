plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.lombok) apply false
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

tasks.register("buildAll") {
    group = "build"
    description = "Builds both Android APK and Desktop JAR"
    dependsOn(":app:assembleRelease", ":desktop:shadowJar")
}

tasks.register("buildDebugAll") {
    group = "build"
    description = "Builds both Android Debug APK and Desktop JAR"
    dependsOn(":app:assembleDebug", ":desktop:shadowJar")
}
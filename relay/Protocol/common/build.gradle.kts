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

    api(libs.netty.buffer)
    api(platform(libs.fastutil.bom))
    api(libs.fastutil.int.obj.maps)
    api(libs.fastutil.obj.int.maps)
    api(libs.math)
}
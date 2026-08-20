@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven("https://repo.opencollab.dev/maven-snapshots")
        maven("https://repo.opencollab.dev/maven-releases")

    }
}

rootProject.name = "WClient"
include(":app")
include(":relay")
include(
    ":relay:Network:codec-query",
    ":relay:Network:codec-rcon",
    ":relay:Network:transport-raknet",
)
// relay:Protocol:bedrock-codec / bedrock-connection / common are no longer built from the local
// vendored source (see relay/build.gradle.kts) - they're pulled from Maven instead so we get
// current Bedrock protocol support. Leaving the source directories in place, just not building them.
// relay:Protocol:adventure was also removed from this list: it pointed at a nonexistent
// "relay/adventure" directory (the real path is "relay/Protocol/adventure") and nothing in the
// project actually depended on it as a project(...), so it was dead/broken either way.

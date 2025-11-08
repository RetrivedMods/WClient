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
include(":desktop")
include(
    ":relay:adventure",
    ":relay:Protocol:bedrock-codec",
    ":relay:Protocol:bedrock-connection",
    ":relay:Protocol:common",
    ":relay:Network:codec-query",
    ":relay:Network:codec-rcon",
    ":relay:Network:transport-raknet",
)

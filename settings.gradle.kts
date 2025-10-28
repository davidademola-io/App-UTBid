pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        // you likely already have these two:
        id("com.android.application") version "8.7.0"
        kotlin("android") version "2.0.21"

        // 👇 add this line
        kotlin("plugin.compose") version "2.0.21"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "App-UTBid"
include(":app")

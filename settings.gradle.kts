pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Build cache configuration for improved build performance
buildCache {
    local {
        // Set directory to "<project-root>/.gradle/build-cache"
        // directory = File(rootDir, ".gradle/build-cache") // Or your preferred location
        // enable by default
        isEnabled = true
    }
}

rootProject.name = "Katalis"
include(":app")
 
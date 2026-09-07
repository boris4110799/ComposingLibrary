import tw.boris4110799.composing.convention.dsl.ComposingMavenRepository

rootProject.name = "ComposingLibrary"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/boris4110799/ComposingBuildLogic")

            val properties = java.util.Properties().apply {
                load(java.io.FileInputStream(file("github.properties")))
            }

            credentials {
                username = properties.getProperty("username", "")
                password = properties.getProperty("password", "")
            }
        }
    }
    plugins {
        id("tw.boris4110799.composing.convention.multiplatformCompose") version "1.0.0"
        id("tw.boris4110799.composing.convention.multiplatformLibrary") version "1.0.0"
        id("tw.boris4110799.composing.convention.multiplatformMavenPublish") version "1.0.0"
    }
}

plugins {
    id("tw.boris4110799.composing.convention.settings") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

composing {
    basePackage = "tw.boris4110799.composing"

    maven {
        description = "The Library of Composing Project"

        pom {
            developerId = "boris4110799"
            developerName = "BorisHuang"
        }
        repository {
            name = "Github"
            url = "https://maven.pkg.github.com/boris4110799/ComposingLibrary"
            credentialsFile = ComposingMavenRepository.GITHUB_CREDENTIALS_FILE
        }
    }
}

include(":library-common")
include(":library-ui")

# Composing Library

The Library of Composing Project.

## Usage

```kotlin
// Settings Gradle
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/boris4110799/ComposingLibrary")
            credentials {
                username = "" // Fill in with your name.
                password = "" // Fill in with your PAT.
            }
        }
    }
}

// App Gradle
dependencies {
    implementation("tw.boris4110799.composing:library-common:0.1.0")
    implementation("tw.boris4110799.composing:library-ui:0.1.0")
}
```

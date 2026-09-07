plugins {
    alias(libs.plugins.convention.multiplatformLibrary)
    alias(libs.plugins.convention.composeMultiplatform)
    alias(libs.plugins.convention.mavenPublish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Android
            implementation(libs.androidx.navigation3.runtime)

            // CMP
            implementation(libs.cmp.lifecycle.navigation3)
            implementation(libs.cmp.lifecycle.runtime)
            implementation(libs.cmp.lifecycle.viewmodel)

            // Kotlin
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io)
        }
    }
}

plugins {
    alias(libs.plugins.convention.multiplatformLibrary)
    alias(libs.plugins.convention.composeMultiplatform)
    alias(libs.plugins.convention.mavenPublish)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // CMP
            implementation(libs.cmp.foundation)
            implementation(libs.cmp.lifecycle.navigation3)
            implementation(libs.cmp.lifecycle.runtime)
            implementation(libs.cmp.material3)
            implementation(libs.cmp.ui)
            implementation(libs.cmp.ui.toolingPreview)

            // Kotlin
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.compose)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.cmp.ui.tooling)
}

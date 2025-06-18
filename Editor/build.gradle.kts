import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

group = "apps.chocolatecakecodes.bluebeats"
version = "0.1.0"

repositories {
    mavenCentral()

    google {
        mavenContent {
            includeGroupAndSubgroups("androidx")
            includeGroupAndSubgroups("com.android")
            includeGroupAndSubgroups("com.google")
        }
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(project(":PlSerialization"))

            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationJsonIo)
            implementation(libs.kotlinxIo)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.desktop.currentOs)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation(libs.filekit.dialogs.compose)
            implementation(libs.bonsai.core)
        }
    }
}

compose.desktop {
    application {
        mainClass = "apps.chocolatecakecodes.bluebeats.mpv.editor.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "${project.group}.mpv.editor"
            packageVersion = project.version as String
        }
    }
}

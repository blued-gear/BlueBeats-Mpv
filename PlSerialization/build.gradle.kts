plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "apps.chocolatecakecodes.bluebeats.mpv.internal"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            api("apps.chocolatecakecodes.bluebeats:BluePlaylists:+")

            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationJsonIo)
        }
    }
}

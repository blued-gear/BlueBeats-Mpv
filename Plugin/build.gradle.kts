import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import java.net.URI
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBinary as KNativeBinary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "apps.chocolatecakecodes.bluebeats"
version = "0.1.0"

repositories {
    mavenCentral()

    mavenLocal {
        this.setUrl(rootProject.layout.projectDirectory.dir("../BluePlaylists/mavenrepo"))
    }
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            sharedLib {
                val mpvIncludeDir = project.layout.buildDirectory.dir("deps/mpv/include").get().asFile.absolutePath
                linkerOpts.add("-L$mpvIncludeDir")

                linkProj(this, ":Taglib:TaglibCpp")
                linkProj(this, ":Taglib:TaglibGlue")
            }
        }

        compilations.getByName("main") {
            cinterops {
                this.create("mpv") {
                    val includeDir = project.layout.buildDirectory.dir("deps/mpv/include").get().asFile.absolutePath

                    this.packageName = "mpv"
                    this.compilerOpts.apply {
                        add("-I$includeDir")
                    }
                    this.headers("${includeDir}/client.h")
                }
            }
        }
    }

    sourceSets {
        nativeMain.dependencies {
            implementation(project(":Taglib"))

            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationJsonIo)
            implementation(libs.kotlinxIo)
        }
    }
}

tasks.create("downloadMpvHeaders") {
    val mpvVersion = libs.versions.mpv.get()
    val destDir = project.layout.buildDirectory.dir("deps/mpv/include").get()

    group = "prepare"
    inputs.property("mpvVersion", mpvVersion)
    outputs.dir(destDir)

    doLast {
        URI("https://github.com/mpv-player/mpv/archive/refs/tags/v$mpvVersion.zip").toURL().openStream().let { zipIn ->
            ZipInputStream(zipIn).use { zip ->
                var entry: ZipEntry? = zip.nextEntry
                while(entry != null) {
                    if(entry.name == "mpv-$mpvVersion/include/mpv/client.h") break
                    entry = zip.nextEntry
                }

                Files.newOutputStream(destDir.file("client.h").asFile.toPath()).use { out ->
                    zip.transferTo(out)
                }
            }
        }
    }
}

tasks.getByName("cinteropMpvNative") {
    dependsOn("downloadMpvHeaders")
}

fun linkProj(target: KNativeBinary, project: String) {
    val variant = when(target.buildType) {
        NativeBuildType.RELEASE -> "release"
        NativeBuildType.DEBUG -> "debug"
    }
    val proj = project(project)
    val binDir = proj.layout.buildDirectory.dir("lib/main/$variant").get().asFile.absolutePath

    target.linkerOpts.add("-L$binDir")
    target.linkerOpts.add("-l${proj.name}")

    tasks.getByName("link${when(target.buildType){NativeBuildType.RELEASE -> "Release"; NativeBuildType.DEBUG -> "Debug"}}SharedNative") {
        this.inputs.file("$binDir/lib${proj.name}.a")
    }
}

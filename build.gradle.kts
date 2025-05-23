import java.net.URI
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "apps.chocolatecakecodes.bluebeats"
version = "0.1.0"

repositories {
    mavenCentral()

    mavenLocal {
        this.setUrl(layout.projectDirectory.dir("../BluePlaylists/mavenrepo"))
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
                linkerOpts.apply {
                    add("-L$mpvIncludeDir")
                }
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
            implementation("apps.chocolatecakecodes.bluebeats:BluePlaylists:+")

            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationJsonIo)
        }
    }
}

tasks.create<Copy>("copyPluginToContainer") {
    val useDebug = true

    dependsOn(if(useDebug) "linkDebugSharedNative" else "linkReleaseSharedNative")

    from(project.layout.buildDirectory.file("bin/native/${if(useDebug) "debug" else "release"}Shared/libBlueBeatsMpv.so"))
    into(project.layout.projectDirectory.dir("container").dir("plugins"))
}

tasks.create<Exec>("runContainer") {
    group = "run"
    dependsOn("copyPluginToContainer")

    workingDir(project.layout.projectDirectory.dir("container"))
    commandLine("./run.sh")
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

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

repositories {
    mavenCentral()
}

group = "apps.chocolatecakecodes.bluebeats.mpv.internal"
version = "1.0.0"

project.evaluationDependsOnChildren()

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
            staticLib()
        }

        compilations.getByName("main") {
            cinterops {
                this.create("libtag") {
                    this.packageName = "taglib"
                    val includeDir = project("TaglibGlue").layout.projectDirectory.dir("src/main/headers").asFile.absolutePath

                    this.compilerOpts.apply {
                        add("-I$includeDir")
                    }

                    this.headers("${includeDir}/taglib_glue.h")
                }
            }
        }
    }

    sourceSets {
        nativeMain {
            dependencies {
                api("apps.chocolatecakecodes.bluebeats:BluePlaylists:+")
            }
        }
    }
}

tasks.getByName("compileKotlinNative") {
    dependsOn("TaglibGlue:build")
}

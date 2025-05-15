plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

group = "apps.chocolatecakecodes.bluebeats"
version = "0.1.0"

repositories {
    mavenCentral()
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
            staticLib {}
        }

        compilations.getByName("main") {
            cinterops {
                val libvlccore by creating
            }
        }
    }

    sourceSets {
        nativeMain.dependencies {
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxSerializationJsonIo)
        }
    }
}

tasks.create<Exec>("buildVlcModule") {
    group = "build"
    dependsOn("assemble")

    workingDir(project.layout.projectDirectory.dir("plugin"))
    commandLine("make", "clean", "build")
}

tasks.named("build") {
    dependsOn("buildModule")
}

tasks.create<Copy>("copyVlcModuleToContainer") {
    dependsOn("buildVlcModule")

    from(project.layout.projectDirectory.dir("plugin").file("libbluebeats_plugin.so"))
    into(project.layout.projectDirectory.dir("container").dir("plugins"))
}

tasks.create<Exec>("runContainer") {
    group = "run"
    dependsOn("copyVlcModuleToContainer")

    workingDir(project.layout.projectDirectory.dir("container"))
    commandLine("./run.sh")
}

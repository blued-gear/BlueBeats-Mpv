plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
}

allprojects {
    repositories {
        mavenLocal {
            this.setUrl(rootProject.layout.projectDirectory.dir("../BluePlaylists/mavenrepo"))
        }
    }
}

tasks.create<Copy>("copyPluginToContainer") {
    val useDebug = true

    dependsOn(if(useDebug) ":Plugin:linkDebugSharedNative" else ":Plugin:linkReleaseSharedNative")

    from(project("Plugin").layout.buildDirectory.file("bin/native/${if(useDebug) "debug" else "release"}Shared/libPlugin.so"))
    into(project.layout.projectDirectory.dir("container").dir("plugins"))
    rename { "libBlueBeatsMpv.so" }
}

tasks.create<Exec>("runContainer") {
    group = "run"
    dependsOn("copyPluginToContainer")

    workingDir(project.layout.projectDirectory.dir("container"))
    commandLine("./run.sh")
}

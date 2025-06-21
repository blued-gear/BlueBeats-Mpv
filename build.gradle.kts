import org.gradle.internal.extensions.stdlib.uncheckedCast
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.Path
import kotlin.io.path.inputStream

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
    val useDebug = false

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

tasks.create("publishPlugin") {
    group = "publishing"
    dependsOn(":Plugin:linkReleaseSharedNative")
    outputs.upToDateWhen { false }

    doLast {
        val proj = project("Plugin")
        val file = proj.layout.buildDirectory.file("bin/native/releaseShared/libPlugin.so").get().asFile.absolutePath
        publishArtifact("Plugin", proj.version.toString(), "bluebeats.so", file)
    }
}

tasks.create("publishEditor") {
    group = "publishing"
    dependsOn(":Editor:packageReleaseUberJarForCurrentOS")
    outputs.upToDateWhen { false }

    doLast {
        val proj = project("Editor")
        val version = proj.version.toString()
        val file = proj.layout.buildDirectory.file("compose/jars/apps.chocolatecakecodes.bluebeats.mpv.editor-linux-x64-$version.jar").get().asFile.absolutePath
        publishArtifact("Editor", version, "BlueBeats-Editor.jar", file)
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun publishArtifact(name: String, version: String, filename: String, path: String) {
    val props = Properties().apply {
        project.rootProject.file("local.properties").inputStream().use {
            this.load(it)
        }
    }

    val url = "${props.getProperty("gitlab.host")}/api/v4/projects/${props.getProperty("gitlab.projectId")}/packages/generic/$name/$version/$filename"
    val authCredentials = "${props.getProperty("gitlab.publish.username")}:${props.getProperty("gitlab.publish.password")}"

    @Suppress("DEPRECATION")
    val http = URL(url).openConnection().uncheckedCast<HttpURLConnection>().apply {
        doOutput = true
        doInput = true
        instanceFollowRedirects = true
        requestMethod = "PUT"
        setRequestProperty("Authorization", "Basic " + Base64.Mime.encode(authCredentials.toByteArray()))
    }

    logger.lifecycle("uploading artifact $path")
    Path(path).inputStream().use {
        it.transferTo(http.outputStream)
        http.outputStream.flush()
        http.outputStream.close()
    }

    val respCode = http.responseCode
    if(respCode < 200 || respCode >= 300) {
        val err = http.errorStream.bufferedReader().use { it.readText() }
        throw IOException("Upload of artifact $name failed: statusCode = $respCode; message = $err")
    } else {
        logger.lifecycle("Uploaded artifact $name")
    }
}


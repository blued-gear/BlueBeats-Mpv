/*NOTE
    Kotlin Native can only import C libraries, but Taglib is C++.
    So this library glues them together by providing a C interface for needed functions.
 */

plugins {
    `cpp-library`
}

group = "apps.chocolatecakecodes.bluebeats.mpv.internal"
version = "1"

library {
    linkage = listOf(Linkage.STATIC)
}

tasks.withType<CppCompile> {
    dependsOn(":Taglib:TaglibCpp:build")

    // tell the compiler to look into every dir for Taglib headers
    fileTree(project.projectDir.resolve("../TaglibCpp/src/headers")).map {
        "-I" + it.parentFile.absolutePath
    }.distinct().let {
        this.compilerArgs.addAll(it)
    }

    this.compilerArgs.addAll("-DHAVE_CONFIG_H", "-DTAGLIB_STATIC", "-fPIC")
}

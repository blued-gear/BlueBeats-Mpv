pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "BlueBeatsMpv"

include("Plugin")
include("Taglib")
include("Taglib:TaglibCpp")
include("Taglib:TaglibGlue")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = "rewrite-cobol"



include("cobol-cli")

val isCiServer = System.getenv("CI")?.equals("true") ?: false



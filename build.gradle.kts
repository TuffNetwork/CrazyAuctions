import utils.convertList
import utils.updateMarkdown

plugins {
    id("modrinth-plugin")
    id("hangar-plugin")

    `java-plugin`
}

val branch = "main"
val hash = "0000000"
val commit = "Manual Build"

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        subprojects {
            dependsOn(project.tasks.build)
        }

        archiveClassifier = ""

        val files = subprojects.filter { it.name != "common" && it.name != "api" }.mapNotNull {
            val file = it.tasks.jar.get().archiveFile

            if (file.isPresent) {
                zipTree(file.get().asFile)
            } else {
                null
            }
        }

        from(files) {
            exclude("META-INF/MANIFEST.MF")
        }

        doFirst {
            files.forEach { file ->
                file.matching { include("META-INF/MANIFEST.MF") }.files.forEach {
                    manifest.from(it)
                }
            }
        }
    }
}

val releaseType = rootProject.ext.get("release_type").toString()
val color = rootProject.property("${releaseType.lowercase()}_color").toString()
val isRelease = releaseType.equals("release", true)
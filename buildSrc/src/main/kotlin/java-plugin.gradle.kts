plugins {
    `java-library`
}

repositories {
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.triumphteam.dev/snapshots/")
    maven("https://repo.crazycrew.us/libraries/")
    maven("https://repo.crazycrew.us/releases/")
    maven("https://jitpack.io/")
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        duplicatesStrategy = DuplicatesStrategy.WARN
        val props = mapOf(
            "name" to rootProject.name,
            "version" to rootProject.version,
            "description" to (rootProject.description ?: ""),
            "minecraft" to libs.findVersion("minecraft").get(),
            "website" to "https://github.com/${rootProject.property("repository_owner")}/${rootProject.name}",
            "group" to project.group
        )
        inputs.properties(props)
        with(copySpec {
            include("*paper-plugin.yml", "*plugin.yml")
            from("src/main/resources") { expand(props) }
        })
    }
}

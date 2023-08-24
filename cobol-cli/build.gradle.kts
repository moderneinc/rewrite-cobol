import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("org.openrewrite.build.publish")
    id("org.openrewrite.build.shadow")
    id("org.openrewrite.build.metadata")
}

group = "org.openrewrite"
description = "A version of the Moderne CLI specialized for parsing COBOL LSTs."

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
    val astPublishUsername = project.properties["ast.publish.username"] as String? ?: System.getenv("AST_PUBLISH_USERNAME")
    val astPublishPassword = project.properties["ast.publish.password"] as String? ?: System.getenv("AST_PUBLISH_PASSWORD")
    if (astPublishUsername != null && astPublishPassword != null) {
        maven {
            name = "ModerneArtifactory"
            url = uri("https://artifactory.moderne.ninja/artifactory/moderne-private")
            credentials {
                username = astPublishUsername
                password = astPublishPassword
            }
        }
    }
}

var astWrite = configurations.create("astWrite")
configurations {
    all {
        resolutionStrategy {
            exclude("ch.qos.logback")
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")

    compileOnly("io.moderne:moderne-ast-write:latest.release")
    "astWrite"("io.moderne:moderne-ast-write:latest.release:obfuscated")

    implementation("info.picocli:picocli:latest.release")
    annotationProcessor("info.picocli:picocli-codegen:latest.release")

    implementation("org.slf4j:slf4j-nop:latest.release")
    implementation("com.konghq:unirest-java:3.14.2")
    implementation("org.jline:jline:latest.release")
    implementation("org.fusesource.jansi:jansi:latest.release")
    implementation("org.openrewrite:rewrite-core:latest.integration")
    implementation("org.openrewrite.recipe:rewrite-all:latest.integration")
    implementation(rootProject)

    testRuntimeOnly("io.moderne:moderne-ast-write:latest.release")
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    exclude("**/Build.java", "**/DefaultProgressBar.java")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "utf8"
}

tasks.withType<ShadowJar> {
    manifest {
        attributes("Main-Class" to "io.moderne.cobol.Mod")
    }
    configurations = listOf(project.configurations.getByName("runtimeClasspath"), project.configurations.getByName("astWrite"))
    isZip64 = true
}
artifacts {
    add("runtimeClasspath", tasks.named<ShadowJar>("shadowJar"))
}

val generateVersionFile = tasks.register("generateVersionFile") {
    val outputFile = file("src/main/resources/cli-version.txt")
    val version = project.version.toString()
    description = "Creates $outputFile"
    group = "Build"
    inputs.property("version", version)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(version)
    }
}

val generateAstWriteVersionFile = tasks.register("generateAstWriteVersionFile") {
    val outputFile = file("src/main/resources/ast-write-version.txt")
    val version = astWrite.resolvedConfiguration.firstLevelModuleDependencies.iterator().next().moduleVersion

    description = "Creates $outputFile"
    group = "Build"
    inputs.property("version", version)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        outputFile.writeText(version)
    }
}

tasks.named<Copy>("processResources") {
    dependsOn(generateVersionFile, generateAstWriteVersionFile)
}
tasks.named<Jar>("sourcesJar") {
    dependsOn(generateVersionFile, generateAstWriteVersionFile)
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.google.cloud.artifactregistry.gradle-plugin") version "latest.release"
    id("org.openrewrite.build.publish")
    id("org.openrewrite.build.shadow")
}

group = "org.openrewrite"
description = "A version of the Moderne CLI specialized for parsing COBOL LSTs."

repositories {
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
    maven {
        url = uri("artifactregistry://us-west1-maven.pkg.dev/moderne-dev/moderne-releases")
    }
}

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

    implementation("info.picocli:picocli:latest.release")
    compileOnly("io.moderne:moderne-ast-write:latest.release")
    annotationProcessor("info.picocli:picocli-codegen:latest.release")

    implementation("org.slf4j:slf4j-nop:latest.release")
    implementation("com.konghq:unirest-java:3.14.2")
    implementation("org.jline:jline:latest.release")
    implementation("org.fusesource.jansi:jansi:latest.release")
    implementation("org.openrewrite:rewrite-core:latest.integration")
    implementation("org.openrewrite.recipe:rewrite-all:latest.integration")
    implementation(rootProject)

    testRuntimeOnly("io.moderne:moderne-ast-write:latest.release:obfuscated")
}

application {
    mainClass.set("io.moderne.cobol.Mod")
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

tasks.withType<ShadowJar> {
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))
    isZip64 = true
}

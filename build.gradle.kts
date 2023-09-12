plugins {
    id("org.openrewrite.build.root") version("latest.release")
    id("org.openrewrite.build.language-library") version("latest.release")
    id("com.netflix.nebula.integtest-standalone") version "10.1.5"
}

group = "org.openrewrite"
description = "Rewrite support for the COBOL language"

tasks.register<JavaExec>("generateAntlrSourcesCobol") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
        "-o", "src/main/java/org/openrewrite/cobol/internal/grammar",
        "-package", "org.openrewrite.cobol.internal.grammar",
        "-visitor"
    ) + fileTree("src/main/antlr-cobol").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("generateAntlrSourcesJcl") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/jcl/internal/grammar",
            "-package", "org.openrewrite.jcl.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-jcl").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("generateAntlrSourcesControlM") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
        "-o", "src/main/java/org/openrewrite/controlm/internal/grammar",
        "-package", "org.openrewrite.controlm.internal.grammar",
        "-visitor"
    ) + fileTree("src/main/antlr-controlm").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath
}

sourceSets {
    create("model") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val modelImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val modelAnnotationProcessor: Configuration by configurations.getting
val modelCompileOnly: Configuration by configurations.getting

configurations["modelRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val latest = if (project.hasProperty("releasing")) {
    "latest.release"
} else {
    "latest.integration"
}

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    compileOnly("org.openrewrite:rewrite-test")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    implementation(platform("org.openrewrite:rewrite-bom:${latest}"))
    implementation("org.openrewrite:rewrite-core")
    implementation("org.antlr:antlr4:4.11.1")
    implementation("io.micrometer:micrometer-core:1.9.+")
    implementation("io.github.classgraph:classgraph:latest.release")
    runtimeOnly("org.openrewrite.tools:java-object-diff:latest.release")

    modelImplementation("org.openrewrite:rewrite-java-17")
    modelAnnotationProcessor("org.projectlombok:lombok:latest.release")
    modelCompileOnly("org.projectlombok:lombok:latest.release")
    modelImplementation("ch.qos.logback:logback-classic:latest.release")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testImplementation("org.junit-pioneer:junit-pioneer:2.0.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.assertj:assertj-core:latest.release")

    testImplementation("org.openrewrite:rewrite-groovy")
    testImplementation("org.openrewrite:rewrite-maven")
    testImplementation("org.openrewrite:rewrite-xml")
}

configure<nl.javadude.gradle.plugins.license.LicenseExtension> {
    excludePatterns.add("**/*.CBL")
    excludePatterns.add("**/*.CPY")
}

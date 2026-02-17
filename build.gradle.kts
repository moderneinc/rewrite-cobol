plugins {
    id("org.openrewrite.build.root") version("latest.release")
    id("org.openrewrite.build.language-library") version("latest.release")
    id("com.netflix.nebula.integtest-standalone") version "10.1.5"
    id("org.openrewrite.build.moderne-proprietary-license") version "latest.release"
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

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesJcl") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/jcl/internal/grammar",
            "-package", "org.openrewrite.jcl.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-jcl").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesControlM") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
        "-o", "src/main/java/org/openrewrite/controlm/internal/grammar",
        "-package", "org.openrewrite.controlm.internal.grammar",
        "-visitor"
    ) + fileTree("src/main/antlr-controlm").matching { include("**/*.g4") }.map { it.path }

    classpath = sourceSets["main"].runtimeClasspath

    finalizedBy("licenseFormat")
}

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
    implementation("org.antlr:antlr4:4.13.2")
    implementation("io.micrometer:micrometer-core:1.9.+")
    implementation("io.github.classgraph:classgraph:latest.release")
    runtimeOnly("org.openrewrite.tools:java-object-diff:latest.release")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.14.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.14.0")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.14.0")

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
tasks.withType<com.hierynomus.gradle.license.tasks.LicenseCheck>().configureEach {
    enabled = false;
}

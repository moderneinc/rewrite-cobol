plugins {
    id("org.openrewrite.build.root") version("latest.release")
    id("org.openrewrite.build.language-library") version("latest.release")
    id("com.netflix.nebula.integtest-standalone") version "10.1.5"
    id("org.openrewrite.build.moderne-source-available-license") version "latest.release"
}

group = "org.openrewrite"
description = "Rewrite support for the COBOL language"

val antlr by configurations.creating

tasks.register<JavaExec>("generateAntlrSourcesCobol") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
        "-o", "src/main/java/org/openrewrite/cobol/internal/grammar",
        "-package", "org.openrewrite.cobol.internal.grammar",
        "-visitor"
    ) + fileTree("src/main/antlr-cobol").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesJcl") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/jcl/internal/grammar",
            "-package", "org.openrewrite.jcl.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-jcl").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesBms") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/bms/internal/grammar",
            "-package", "org.openrewrite.bms.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-bms").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesIms") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/ims/internal/grammar",
            "-package", "org.openrewrite.ims.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-ims").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesDb2") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/db2/internal/grammar",
            "-package", "org.openrewrite.db2.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-db2").matching { include("**/*.g4") }.map { it.path }
}

tasks.register<JavaExec>("generateAntlrSourcesBind") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/db2/bind/internal/grammar",
            "-package", "org.openrewrite.db2.bind.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-bind").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesLinkEdit") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/linkedit/internal/grammar",
            "-package", "org.openrewrite.linkedit.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-linkedit").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesSort") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/controlcard/sort/internal/grammar",
            "-package", "org.openrewrite.controlcard.sort.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-sort").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesIdcams") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
            "-o", "src/main/java/org/openrewrite/controlcard/idcams/internal/grammar",
            "-package", "org.openrewrite.controlcard.idcams.internal.grammar",
            "-visitor"
    ) + fileTree("src/main/antlr-idcams").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

    finalizedBy("licenseFormat")
}

tasks.register<JavaExec>("generateAntlrSourcesControlM") {
    mainClass.set("org.antlr.v4.Tool")
    args = listOf(
        "-o", "src/main/java/org/openrewrite/controlm/internal/grammar",
        "-package", "org.openrewrite.controlm.internal.grammar",
        "-visitor"
    ) + fileTree("src/main/antlr-controlm").matching { include("**/*.g4") }.map { it.path }

    classpath = configurations["antlr"]

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
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("io.micrometer:micrometer-core:1.9.+")
    implementation("io.github.classgraph:classgraph:latest.release")
    runtimeOnly("org.openrewrite.tools:java-object-diff:latest.release")
    testImplementation("org.junit-pioneer:junit-pioneer:2.3.0")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.assertj:assertj-core:latest.release")

    testImplementation("org.openrewrite:rewrite-groovy")
    testImplementation("org.openrewrite:rewrite-maven")
    testImplementation("org.openrewrite:rewrite-xml")
}

// ExportScaleTest builds a two-million-line Control-M export in memory; nothing else here needs it.
tasks.withType<Test>().configureEach {
    if (System.getenv("CONTROLM_SCALE") != null) {
        maxHeapSize = "10g"
    }
}

// The corpus is invisible to the build otherwise, so a green run gets replayed over a corpus that grew.
tasks.test {
    val corpora = listOf("COBOL_CORPUS", "JCL_CORPUS", "BMS_CORPUS", "CONTROLM_CORPUS", "DB2_CORPUS",
        "IMS_CORPUS", "ASM_CORPUS", "SAS_CORPUS")
    corpora.forEach { inputs.property(it, System.getenv(it)).optional(true) }
    inputs.files(corpora.mapNotNull { System.getenv(it) }.distinct()
        .map { fileTree(it) { exclude("**/.git/**", "**/.moderne/**") } })
        .withPropertyName("corpus")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

configure<nl.javadude.gradle.plugins.license.LicenseExtension> {
    excludePatterns.add("**/*.CBL")
    excludePatterns.add("**/*.CPY")
}
tasks.withType<com.hierynomus.gradle.license.tasks.LicenseCheck>().configureEach {
    enabled = false;
}

// This artifact is not published to Maven Central. Disabling rather than unregistering the Sonatype
// tasks means the shared release workflow, which names them explicitly, skips them instead of
// failing on a missing task. Publishing to the Code Genome Project is unaffected.
tasks.matching { it.name.contains("Sonatype") || it.name.endsWith("StagingRepositories") }
    .configureEach { enabled = false }

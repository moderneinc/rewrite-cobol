import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
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
    create("astWrite")
}

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")

    if (System.getProperty("idea.active") != null || System.getProperty("idea.sync.active") != null) {
        implementation("io.moderne:moderne-ast-write:latest.release")
    } else {
        compileOnly("io.moderne:moderne-ast-write:latest.release")
        "astWrite"("io.moderne:moderne-ast-write:latest.release:obfuscated")
    }

    implementation("info.picocli:picocli:latest.release")
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

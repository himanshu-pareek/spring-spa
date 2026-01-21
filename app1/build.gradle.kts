import com.github.gradle.node.npm.task.NpmTask
import org.gradle.kotlin.dsl.register

plugins {
    id("java")
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// START: Node Project Configuration
node {
    download = true
    version = "24.12.0"
}

val buildWebapp = tasks.register<NpmTask>("buildWebapp") {
    dependsOn("npmInstall")
    environment.put("BUILD_PATH", layout.buildDirectory.dir("webapp").get().asFile.absolutePath)
    args.assign(listOf("run", "build"))
    inputs.files("package.json", "package-lock.json")
    inputs.dir("src")
    inputs.dir(fileTree("node_modules").exclude(".cache"))
    outputs.dir(layout.buildDirectory.dir("webapp"))
}

val watchWebapp = tasks.register<NpmTask>("watchWebapp") {
    dependsOn("npmInstall")
    environment.put("BUILD_PATH", "../build/resources/main/templates/app1")
    args.assign(listOf("run", "build:watch"))
}

tasks.processResources {
    dependsOn(buildWebapp)
}

// END: Node Project Configuration
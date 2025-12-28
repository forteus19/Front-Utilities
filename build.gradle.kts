import red.vuis.remaptask.RemapTask

plugins {
    id("dev.architectury.loom") version "1.10-SNAPSHOT"
    id("red.vuis.remap-task") version "1.0-SNAPSHOT"
}

group = "red.vuis"
version = "0.2.3"

val bfVersion = "0.7.1.2b"

repositories {
    maven("https://maven.neoforged.net/releases")
    exclusiveContent {
        forRepository {
            maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven")
        }
        filter {
            includeGroup("software.bernie.geckolib")
        }
    }
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:1.21+build.4")
    })
    neoForge("net.neoforged:neoforge:21.1.211")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    modCompileOnly(files("bf/$bfVersion-named.jar"))
    modRuntimeOnly(files("bf/$bfVersion-original.jar"))

    modCompileOnly("software.bernie.geckolib:geckolib-neoforge-1.21.1:4.7.3")
    compileOnly(files("bf/bflib-$bfVersion.jar"))

    compileOnly("com.demonwav.mcdev:annotations:2.1.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>() {
    options.release = 21
}

val remapBfObfTask = tasks.register<RemapTask>("remapBfObf") {
    dependsOn(tasks.remapJar)
    input = tasks.remapJar.get().archiveFile
    output = base.libsDirectory.file("${project.name}-${project.version}-bfobf.jar")
    mappings = file("bf/$bfVersion-merged.tiny")
    classpath.from(files("bf/$bfVersion-named.jar"))
    from = "named"
    to = "official"
}

tasks.build {
    dependsOn(remapBfObfTask)
}

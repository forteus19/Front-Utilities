import net.neoforged.moddevgradle.internal.RunGameTask

plugins {
    id("net.neoforged.moddev") version "2.0.89"
    id("red.vuis.vbm-plugin") version "1.0-SNAPSHOT"
}

group = "red.vuis"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("bflib-0.7.0.10b.jar"))
}

neoForge {
    version = "21.1.172"

    runs {
        create("client") {
            client()
            taskBefore(tasks.named("remapMod"))
            environment("__GL_THREADED_OPTIMIZATIONS", "0")
        }
    }
}

vbmPlugin {
    bfVersion = "0.7.0.10b"
    mappings = file("0.7.0.10b-merged.tiny")
    runTask(tasks.named<RunGameTask>("runClient"))
}

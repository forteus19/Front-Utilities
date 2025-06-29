import net.neoforged.moddevgradle.internal.RunGameTask

plugins {
    id("net.neoforged.moddev") version "2.0.89"
    id("red.vuis.vbm-plugin") version "1.0-SNAPSHOT"
}

group = "red.vuis"
version = "0.1.1-0.7.0.13b"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("bflib-0.7.0.13b.jar", "geckolib-neoforge-1.21.1-4.7.3.jar"))
}

neoForge {
    version = "21.1.183"

    parchment {
        minecraftVersion = "1.21.1"
        mappingsVersion = "2024.11.17"
    }

    runs {
        create("client") {
            client()
            taskBefore(tasks.named("remapMod"))
            environment("__GL_THREADED_OPTIMIZATIONS", "0")
        }
    }
}

vbmPlugin {
    bfVersion = "0.7.0.13b"
    mappings = file("0.7.0.13b-merged.tiny")
    runTask(tasks.named<RunGameTask>("runClient"))
}

//tasks.remapMod {
//    classpath = classpath.from(layout.buildDirectory.files("moddev/artifacts/neoforge-21.1.172-merged.jar"))
//}

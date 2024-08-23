import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonApi(project(":packages:shulker-proxy-api"))

    // Filesystem
    commonImplementation(libs.apache.commons.io)
    commonImplementation(libs.snakeyaml)

    // Kubernetes
    commonCompileOnly(libs.kubernetes.client.api)
    commonRuntimeOnly(libs.kubernetes.client)
    commonImplementation(libs.kubernetes.client.http)

    // Agones
    commonImplementation(project(":packages:google-agones-sdk"))

    // Sync
    commonImplementation(libs.jedis)
    commonImplementation(libs.guava)
}

setOf("bungeecordJar", "velocityJar").forEach { taskName ->
    tasks.named(taskName, ShadowJar::class.java) {
        mergeServiceFiles()
    }
}

tasks.named("processBungeecordResources", ProcessResources::class.java) {
    inputs.property("version", project.version)
    expand("version" to project.version)
}

ktlint {
    filter {
        exclude {
            it.file.path.contains(layout.buildDirectory.dir("generated").get().toString())
        }
    }
}

buildConfig {
    packageName("io.shulkermc.proxyagent")

    sourceSets.getByName("velocity") {
        buildConfigField("String", "VERSION", "\"${project.version}\"")
    }
}

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    "commonApi"(project(":packages:shulker-server-api"))

    // Agones
    "commonImplementation"(project(":packages:google-agones-sdk-bindings-java"))
    "commonRuntimeOnly"("io.grpc:grpc-netty-shaded:1.59.0")
}

setOf("paperJar").forEach { taskName ->
    tasks.named(taskName, ShadowJar::class.java) {
        mergeServiceFiles()
    }
}

tasks.named("processPaperResources", ProcessResources::class.java) {
    inputs.property("version", project.version)
    expand("version" to project.version)
}

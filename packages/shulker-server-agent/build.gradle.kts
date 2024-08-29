import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    commonApi(project(":packages:shulker-server-api"))

    // Agones
    commonImplementation(project(":packages:google-agones-sdk"))
}

setOf("processPaperResources").forEach { taskName ->
    tasks.named(taskName, ProcessResources::class.java) {
        inputs.property("version", project.version)
        expand("version" to project.version)
    }
}

tasks.withType(ShadowJar::class.java) {
    relocate("com.google.protobuf", "shulker.protobuf")
}

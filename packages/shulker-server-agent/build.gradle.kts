import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    commonApi(project(":packages:shulker-server-api"))
    commonApi(project(":packages:shulker-agent"))

    // Kubernetes
    commonCompileOnly(libs.kubernetes.client.api)
    commonRuntimeOnly(libs.kubernetes.client)
    commonImplementation(libs.kubernetes.client.http)

    // Agones
    commonImplementation(project(":packages:google-agones-sdk"))

    commonImplementation(libs.jedis)
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

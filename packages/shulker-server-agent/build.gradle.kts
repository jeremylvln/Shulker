import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    // Agones
    "commonImplementation"(project(":packages:google-agones-sdk-bindings-java"))
    "commonRuntimeOnly"("io.grpc:grpc-netty-shaded:1.58.0")
}

tasks.named("paperJar", ShadowJar::class.java) {
    dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
    mergeServiceFiles()
}

tasks.named("processPaperResources", ProcessResources::class.java) {
    inputs.property("version", project.version)
    expand("version" to project.version)
}

dependencies {
    commonApi(project(":packages:shulker-server-api"))

    commonImplementation(project(":packages:google-agones-sdk-bindings-java"))
    commonRuntimeOnly("io.grpc:grpc-netty-shaded:1.58.0")
}

tasks {
    paperJar {
        dependsOn(":packages:shulker-server-api:shadowJar")
        dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
        mergeServiceFiles()
    }

    processPaperResources {
        inputs.property("version", project.version)
        expand("version" to project.version)
    }
}

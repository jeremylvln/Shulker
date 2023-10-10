dependencies {
    commonApi(project(":packages:shulker-proxy-api"))

    commonCompileOnly("io.fabric8:kubernetes-client-api:6.3.1")
    commonRuntimeOnly("io.fabric8:kubernetes-client:6.3.1")
    commonImplementation("io.fabric8:kubernetes-httpclient-okhttp:6.2.0")

    commonImplementation(project(":packages:google-agones-sdk-bindings-java"))
    commonRuntimeOnly("io.grpc:grpc-netty-shaded:1.58.0")
}

tasks {
    bungeecordJar {
        dependsOn(":packages:shulker-proxy-api:shadowJar")
        dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
        mergeServiceFiles()
    }

    velocityJar {
        dependsOn(":packages:shulker-proxy-api:shadowJar")
        dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
        mergeServiceFiles()
    }
}

plugins {
    id("com.github.gmazzo.buildconfig") version "4.1.2"
}

dependencies {
    commonApi(project(":packages:shulker-proxy-api"))

    commonCompileOnly("io.fabric8:kubernetes-client-api:6.9.0")
    commonRuntimeOnly("io.fabric8:kubernetes-client:6.9.0")
    commonImplementation("io.fabric8:kubernetes-httpclient-okhttp:6.9.0")

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

    processBungeecordResources {
        inputs.property("version", project.version)
        expand("version" to project.version)
    }
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

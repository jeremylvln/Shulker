import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.gmazzo.buildconfig") version "4.1.2"
}

dependencies {
    // Kubernetes
    "commonCompileOnly"("io.fabric8:kubernetes-client-api:6.9.0")
    "commonRuntimeOnly"("io.fabric8:kubernetes-client:6.9.0")
    "commonImplementation"("io.fabric8:kubernetes-httpclient-okhttp:6.9.0")

    // Agones
    "commonImplementation"(project(":packages:google-agones-sdk-bindings-java"))
    "commonRuntimeOnly"("io.grpc:grpc-netty-shaded:1.58.0")
}

tasks.named("bungeecordJar", ShadowJar::class.java) {
    dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
    mergeServiceFiles()
}

tasks.named("velocityJar", ShadowJar::class.java) {
    dependsOn(":packages:google-agones-sdk-bindings-java:shadowJar")
    mergeServiceFiles()
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

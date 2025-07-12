plugins {
    alias(libs.plugins.buildconfig)
}

dependencies {
    commonApi(project(":packages:shulker-proxy-api"))
    commonApi(project(":packages:shulker-cluster-api-impl"))

    // Agones
    commonImplementation(project(":packages:google-agones-sdk"))

    // Kubernetes
    commonCompileOnly(libs.kubernetes.client.api)
    commonRuntimeOnly(libs.kubernetes.client)
    commonImplementation(libs.kubernetes.client.http)

    // Utils
    commonImplementation(libs.apache.commons.io)
    commonImplementation(libs.snakeyaml)
    commonImplementation(libs.guava)
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

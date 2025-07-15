plugins {
    alias(libs.plugins.buildconfig)
}

dependencies {
    // Shulker
    commonImplementation(project(":packages:shulker-proxy-api"))
    commonImplementation(project(":packages:shulker-cluster-api-impl"))

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
    packageName("io.shulkermc.proxy")

    sourceSets.getByName("velocity") {
        buildConfigField("String", "VERSION", "\"${project.version}\"")
    }
}

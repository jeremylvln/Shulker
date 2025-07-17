import java.time.Instant

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

    useKotlinOutput {
        internalVisibility = false
    }

    sourceSets.getByName("common") {
        className("BuildConfig")
        buildConfigField("VERSION", project.version.toString())
        buildConfigField("BUILD_TIME", Instant.now().toString())
    }
}

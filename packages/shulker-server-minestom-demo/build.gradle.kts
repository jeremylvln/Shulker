dependencies {
    implementation(libs.minestom)
    implementation(project(":packages:shulker-server-agent", "archives"))
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "io.shulkermc.minestomdemo.MinestomDemo"
        }
    }

    shadowJar {
        dependsOn(":packages:shulker-server-agent:shadowJar")
        mergeServiceFiles()
    }
}

dependencies {
    implementation(libs.minestom)
    implementation(files("$rootDir/dist/java/packages/shulker-server-agent/libs/shulker-server-agent-$version-minestom.jar"))
}

tasks {
    compileJava {
        dependsOn(":packages:shulker-server-agent:minestomJar")
    }

    jar {
        manifest {
            attributes["Main-Class"] = "io.shulkermc.minestomdemo.MinestomDemo"
        }
    }

    shadowJar {
        mergeServiceFiles()
    }
}

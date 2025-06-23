configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":packages:shulker-agent-api"))
}

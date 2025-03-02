dependencies {
    implementation(project(":packages:google-agones-sdk"))
}
configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnlyApi(project(":packages:shulker-sdk"))
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.text.serializer.gson)
}

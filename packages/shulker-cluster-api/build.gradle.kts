configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    compileOnlyApi(libs.adventure.api)
    compileOnlyApi(libs.adventure.text.serializer.gson)
}

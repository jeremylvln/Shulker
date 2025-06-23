configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":packages:shulker-agent-api"))

    // Filesystem
    implementation(libs.apache.commons.io)
    implementation(libs.snakeyaml)

    // Kubernetes
    compileOnly(libs.kubernetes.client.api)
    runtimeOnly(libs.kubernetes.client)
    implementation(libs.kubernetes.client.http)

    // Agones
    implementation(project(":packages:google-agones-sdk"))

    // Sync
    implementation(libs.jedis)
    implementation(libs.guava)
}

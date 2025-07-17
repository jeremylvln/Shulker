dependencies {
    api(project(":packages:shulker-cluster-api"))

    // Agones
    api(project(":packages:google-agones-sdk"))

    // Kubernetes
    compileOnlyApi(libs.kubernetes.client.api)
    runtimeOnly(libs.kubernetes.client)
    implementation(libs.kubernetes.client.http)

    // Cache & PubSub
    implementation(libs.jedis)
}

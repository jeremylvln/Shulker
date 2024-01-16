dependencies {
    api(libs.protobuf)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.services)
    implementation(libs.grpc.stub)
    compileOnly(libs.annotations.api)
    runtimeOnly(libs.grpc.netty.shaded)
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

sourceSets {
    main {
        java {
            srcDir("src/generated/java")
            srcDir("src/generated/grpc")
        }
    }
}

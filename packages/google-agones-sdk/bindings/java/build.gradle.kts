dependencies {
    api("com.google.protobuf:protobuf-java:3.25.0")
    implementation("io.grpc:grpc-protobuf:1.58.0")
    implementation("io.grpc:grpc-services:1.58.0")
    implementation("io.grpc:grpc-stub:1.58.0")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
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

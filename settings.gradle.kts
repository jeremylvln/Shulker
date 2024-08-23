rootProject.name = "shulker"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.25")
            version("kubernetes-client", "6.13.3")
            version("grpc", "1.66.0")

            library("adventure-api", "net.kyori:adventure-api:4.17.0")
            library("adventure-platform-bungeecord", "net.kyori:adventure-platform-bungeecord:4.3.4")
            library("annotations-api", "org.apache.tomcat:annotations-api:6.0.53")
            library("apache-commons-io", "commons-io:commons-io:2.16.1")
            library("bungeecord-api", "net.md-5:bungeecord-api:1.21-R0.1-SNAPSHOT")
            library("folia-api", "dev.folia:folia-api:1.20.6-R0.1-SNAPSHOT")
            library("guava", "com.google.guava:guava:33.3.0-jre")
            library("grpc-common-protos", "com.google.api.grpc:proto-google-common-protos:2.42.0")
            library("grpc-netty-shaded", "io.grpc", "grpc-netty-shaded").versionRef("grpc")
            library("grpc-protobuf", "io.grpc", "grpc-protobuf").versionRef("grpc")
            library("grpc-services", "io.grpc", "grpc-services").versionRef("grpc")
            library("grpc-stub", "io.grpc", "grpc-stub").versionRef("grpc")
            library("jedis", "redis.clients:jedis:5.1.5")
            library("kubernetes-client", "io.fabric8", "kubernetes-client").versionRef("kubernetes-client")
            library("kubernetes-client-api", "io.fabric8", "kubernetes-client-api").versionRef("kubernetes-client")
            library("kubernetes-client-http", "io.fabric8", "kubernetes-httpclient-okhttp").versionRef("kubernetes-client")
            library("protobuf", "com.google.protobuf:protobuf-java:3.25.4")
            library("snakeyaml", "org.yaml:snakeyaml:2.2")
            library("velocity-api", "com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

            plugin("buildconfig", "com.github.gmazzo.buildconfig").version("5.4.0")
            plugin("shadow", "com.gradleup.shadow").version("8.3.0")
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("12.1.1")
            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.6")
        }
    }
}

fun includeBindingProject(name: String) {
    include(":packages:$name")
    project(":packages:$name").projectDir = file("packages/$name/bindings/java")
}

includeBindingProject("google-agones-sdk")
includeBindingProject("google-open-match-sdk")
includeBindingProject("shulker-sdk")

include(":packages:shulker-proxy-api")
include(":packages:shulker-proxy-agent")

include(":packages:shulker-server-api")
include(":packages:shulker-server-agent")

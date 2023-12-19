rootProject.name = "shulker"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.10")
            version("kubernetes-client", "6.9.1")
            version("grpc", "1.60.0")

            library("adventure-api", "net.kyori:adventure-api:4.14.0")
            library("adventure-platform-bungeecord", "net.kyori:adventure-platform-bungeecord:4.3.1")
            library("annotations-api", "org.apache.tomcat:annotations-api:6.0.53")
            library("bungeecord-api", "net.md-5:bungeecord-api:1.18-R0.1-SNAPSHOT")
            library("folia-api", "dev.folia:folia-api:1.19.4-R0.1-SNAPSHOT")
            library("guava", "com.google.guava:guava:32.1.3-jre")
            library("grpc-common-protos", "com.google.api.grpc:proto-google-common-protos:2.29.0")
            library("grpc-protobuf", "io.grpc", "grpc-protobuf").versionRef("grpc")
            library("grpc-services", "io.grpc", "grpc-services").versionRef("grpc")
            library("grpc-stub", "io.grpc", "grpc-stub").versionRef("grpc")
            library("jedis", "redis.clients:jedis:5.1.0")
            library("kubernetes-client", "io.fabric8", "kubernetes-client").versionRef("kubernetes-client")
            library("kubernetes-client-api", "io.fabric8", "kubernetes-client-api").versionRef("kubernetes-client")
            library("kubernetes-client-http", "io.fabric8", "kubernetes-httpclient-okhttp").versionRef("kubernetes-client")
            library("netty-shaded", "io.grpc:grpc-netty-shaded:1.59.1")
            library("protobuf", "com.google.protobuf:protobuf-java:3.25.1")
            library("velocity-api", "com.velocitypowered:velocity-api:3.1.1")

            plugin("buildconfig", "com.github.gmazzo.buildconfig").version("4.1.2")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.0")
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("11.6.1")
            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.1")
        }
    }
}

fun includeBindingProject(name: String) {
  include(":packages:${name}")
  project(":packages:${name}").projectDir = file("packages/${name}/bindings/java")
}

includeBindingProject("google-agones-sdk")
includeBindingProject("google-open-match-sdk")
includeBindingProject("shulker-sdk")

include(":packages:shulker-proxy-api")
include(":packages:shulker-proxy-agent")

include(":packages:shulker-server-api")
include(":packages:shulker-server-agent")

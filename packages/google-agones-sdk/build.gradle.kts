import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.2"
}

dependencies {
    api("com.google.protobuf:protobuf-java:3.22.2")
    api("io.grpc:grpc-stub:1.54.1")
    api("io.grpc:grpc-protobuf:1.54.1")
    api("javax.annotation:javax.annotation-api:1.3.2")
    runtimeOnly("io.grpc:grpc-netty:1.58.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.6.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.54.1"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
            }
        }
    }
}

tasks.withType<ShadowJar> {
    exclude("**/*.proto")
    mergeServiceFiles()
}

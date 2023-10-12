import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("idea")
    id("java")
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

repositories {
    mavenCentral()
}

subprojects {
    if (project.name === "packages") return@subprojects

    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "io.shulkermc"

    project.layout.buildDirectory.set(File("$rootDir/dist/java${project.path.replace(":", "/")}"))
    java.toolchain.languageVersion = JavaLanguageVersion.of(17)

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
        }

        compileKotlin {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }

        assemble {
            dependsOn("shadowJar")
        }

        shadowJar {
            archiveClassifier.set("")
        }

        jacocoTestReport {
            reports {
                csv.required = false
                html.required = false
            }
        }

        test {
            finalizedBy("jacocoTestReport")
        }

        jacocoTestReport {
            dependsOn("test")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = project.name
                from(components["java"])

                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }

                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }

                pom {
                    name.set(project.name)
                    url.set("https://github.com/jeremylvln/shulker")

                    scm {
                        url.set("https://github.com/jeremylvln/shulker")
                    }

                    issueManagement {
                        url.set("https://github.com/jeremylvln/shulker/issues")
                    }

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("jeremylvln")
                            name.set("Jérémy Levilain")
                            email.set("jeremy@jeremylvln.fr")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Shulker"
                url = uri("https://maven.jeremylvln.fr/artifactory/shulker")
                credentials {
                    username = findProperty("artifactory.username")?.toString() ?: System.getenv("ARTIFACTORY_USERNAME")
                    password = findProperty("artifactory.password")?.toString() ?: System.getenv("ARTIFACTORY_PASSWORD")
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
    }

    fun registerPluginProvider(providerName: String, commonSourceSet: SourceSet) {
        val providerSourceSet = sourceSets.create(providerName) {
            compileClasspath += commonSourceSet.output
            runtimeClasspath += commonSourceSet.output
        }

        configurations
            .filter { it.name.startsWith(providerName) }
            .forEach { it.extendsFrom(configurations.getByName("common${it.name.removePrefix(providerName)}")) }

        val providerJar = tasks.register("${providerName}Jar", ShadowJar::class.java) {
            archiveClassifier = providerName
            from(commonSourceSet.output)
            from(providerSourceSet.output)
            configurations = listOf(project.configurations.getByName("${providerName}RuntimeClasspath"))
        }

        artifacts.add("archives", providerJar)
        tasks.assemble {
            dependsOn(providerJar)
        }

        publishing {
            publications {
                named<MavenPublication>("mavenJava") {
                    artifact(providerJar)
                }
            }
        }
    }

    if (project.hasProperty("isProxyPlugin")) {
        apply(plugin = "org.jetbrains.kotlin.kapt")

        val commonSourceSet = sourceSets.create("common")
        listOf("bungeecord", "velocity").forEach { providerName ->
            registerPluginProvider(providerName, commonSourceSet)
        }

        dependencies {
            "commonCompileOnly"("net.kyori:adventure-api:4.14.0")
            "bungeecordCompileOnly"("net.md-5:bungeecord-api:1.20-R0.1")
            "bungeecordImplementation"("net.kyori:adventure-platform-bungeecord:4.3.0")
            "velocityCompileOnly"("com.velocitypowered:velocity-api:3.1.1")
            "kaptVelocity"("com.velocitypowered:velocity-api:3.1.1")
        }
    } else if (project.hasProperty("isServerPlugin")) {
        val commonSourceSet = sourceSets.create("common")
        listOf("paper").forEach { providerName ->
            registerPluginProvider(providerName, commonSourceSet)
        }

        dependencies {
            "commonCompileOnly"("net.kyori:adventure-api:4.14.0")
            "paperCompileOnly"("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
        }
    }
}

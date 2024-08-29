import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
    id("idea")
    id("java")
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("kapt") version libs.versions.kotlin.get()
    alias(libs.plugins.shadow)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output = layout.buildDirectory.file("reports/detekt/merge.sarif")
}

allprojects {
    if (System.getenv("IS_RELEASE") != "true") {
        version = "$version-SNAPSHOT"
    }
}

subprojects {
    if (listOf("packages").contains(project.name)) return@subprojects

    val libs = rootProject.libs

    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group = "io.shulkermc"

    project.layout.buildDirectory.set(File("$rootDir/dist/java${project.path.replace(":", "/")}"))
    java.toolchain.languageVersion = JavaLanguageVersion.of(21)

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        maven(url = "https://repo.papermc.io/repository/maven-public/")
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
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

    if (!project.hasProperty("javaOnly")) {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = libs.plugins.ktlint.get().pluginId)
        apply(plugin = libs.plugins.detekt.get().pluginId)

        dependencies {
            testImplementation(kotlin("test"))
        }

        tasks {
            compileKotlin {
                kotlinOptions {
                    allWarningsAsErrors = true
                }
            }
        }

        detekt {
            buildUponDefaultConfig = true
            ignoreFailures = true
            config.setFrom("$rootDir/gradle/detekt-config.yml")
            baseline = file("$rootDir/gradle/detekt-baseline.xml")
            basePath = rootDir.absolutePath
        }

        tasks.withType<Detekt>().configureEach {
            reports {
                sarif.required = true
            }

            finalizedBy(detektReportMergeSarif)
        }

        detektReportMergeSarif.configure {
            input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
        }
    }

    if (!project.hasProperty("isLibrary")) {
        apply(plugin = libs.plugins.shadow.get().pluginId)

        tasks {
            assemble {
                dependsOn("shadowJar")
            }

            shadowJar {
                archiveClassifier.set("")
            }
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
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
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
                name = "shulker"

                url =
                    if ((version as String).endsWith("-SNAPSHOT")) {
                        uri("https://maven.jeremylvln.fr/artifactory/shulker-snapshots")
                    } else {
                        uri("https://maven.jeremylvln.fr/artifactory/shulker-releases")
                    }

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

    fun registerPluginProvider(
        providerName: String,
        commonSourceSet: SourceSet,
    ) {
        val providerSourceSet =
            sourceSets.create(providerName) {
                compileClasspath += commonSourceSet.output
                runtimeClasspath += commonSourceSet.output
            }

        configurations
            .filter { it.name.startsWith(providerName) }
            .forEach { it.extendsFrom(configurations.getByName("common${it.name.removePrefix(providerName)}")) }

        val providerJar =
            tasks.register("${providerName}Jar", ShadowJar::class.java) {
                mergeServiceFiles()

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

    if (project.name == "shulker-proxy-agent") {
        apply(plugin = "org.jetbrains.kotlin.kapt")

        val commonSourceSet = sourceSets.create("common")
        setOf("bungeecord", "velocity").forEach { providerName ->
            registerPluginProvider(providerName, commonSourceSet)
        }

        dependencies {
            "commonCompileOnly"(libs.adventure.api)
            "bungeecordCompileOnly"(libs.bungeecord.api)
            "bungeecordImplementation"(libs.adventure.platform.bungeecord)
            "velocityCompileOnly"(libs.velocity.api)
            "kaptVelocity"(libs.velocity.api)
        }
    } else if (project.name == "shulker-server-agent") {
        val commonSourceSet = sourceSets.create("common")
        setOf("paper", "minestom").forEach { providerName ->
            registerPluginProvider(providerName, commonSourceSet)
        }

        dependencies {
            "commonCompileOnly"(libs.adventure.api)
            "paperCompileOnly"(libs.folia.api)
            "minestomCompileOnly"(libs.minestom)
            "minestomImplementation"(libs.snakeyaml)
        }
    }
}

tasks.register("detektAll") {
    allprojects {
        this@register.dependsOn(tasks.withType<Detekt>())
    }
}

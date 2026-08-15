import org.gradle.api.publish.PublishingExtension

plugins {
    alias(libs.plugins.kotlin.jvm.plugin) apply false
    alias(libs.plugins.kotlin.compose.plugin) apply false
    `maven-publish`
}

allprojects {
    group = "br.com.arml.response"
    version = "0.1.0"

    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    plugins.withType<MavenPublishPlugin> {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/albertrml/Response")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}

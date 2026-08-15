plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm.plugin)
    alias(libs.plugins.kotlin.compose.plugin)
}

dependencies {
    api(project(":core"))
    api(libs.androidx.compose.runtime)
    implementation(libs.kotlinx.coroutines.android)
    
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}

composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose-stability.conf"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "response-compose"
        }
    }
}

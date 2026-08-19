plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm.plugin)
}

dependencies {
    api(project(":core"))
    api(libs.retrofit)
    
    testImplementation(project(":test"))
    testImplementation(libs.okhttp)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "response-retrofit"
        }
    }
}

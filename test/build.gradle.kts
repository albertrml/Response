plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm.plugin)
}

dependencies {
    api(project(":core"))
    
    // api because we want the consumer to have access to JUnit assertions 
    // when using our DSL
    api(libs.junit)
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "response-test"
        }
    }
}

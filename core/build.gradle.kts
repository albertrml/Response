plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm.plugin)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.junit.vintage.engine)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "response-core"
        }
    }
}

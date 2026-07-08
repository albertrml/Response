plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0"
    `java-library`
    `maven-publish`
}

group = "br.com.arml.core"
version = "0.1.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    val composeVersion = "1.7.6"
    val junitVersion = "4.13.2"
    val kotlinxCoroutines = "1.11.0"
    val mockkVersion = "1.14.11"

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutines")
    api("androidx.compose.runtime:runtime:$composeVersion")
    
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${kotlinxCoroutines}")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${kotlinxCoroutines}")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("junit:junit:$junitVersion")
    testImplementation(kotlin("test"))

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.3")
}

kotlin {
    jvmToolchain(17)

    sourceSets {
        main {
            kotlin.srcDir("src/main/core")
        }
        test {
            kotlin.srcDir("src/test/core")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "response"
            version = version.toString()
            
            from(components["java"])
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/albertrml/Response")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_PUBLISH_TOKEN")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

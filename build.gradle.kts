plugins {
    alias(libs.plugins.kotlin.jvm.plugin) apply false
    alias(libs.plugins.kotlin.compose.plugin) apply false
}

allprojects {
    group = "br.com.arml.response"
    version = "0.1.0"

    repositories {
        mavenCentral()
        google()
    }
}

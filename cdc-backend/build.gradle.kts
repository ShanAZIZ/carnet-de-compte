plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(ktorLibs.plugins.ktor)
}

group = "site.cdc"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.jdbc)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.di)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.kotlin.csv)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.testcontainers.postgresql)
}

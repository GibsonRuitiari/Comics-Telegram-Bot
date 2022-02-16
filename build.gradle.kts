import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}


group = "me.ruitiari"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://mvn.mchv.eu/repository/mchv/")

}

dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.9.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
    implementation("com.github.elbekD", "kt-telegram-bot", "1.3.8")

    // zip4j
    implementation("net.lingala.zip4j","zip4j","2.9.1")

    // config
    implementation("com.natpryce", "konfig", "1.6.10.0" )
    //deleting files at once
    implementation("commons-io:commons-io:2.11.0")

    // logging
    implementation("org.slf4j", "slf4j-simple", "2.0.0-alpha2")
    implementation("io.github.microutils", "kotlin-logging", "2.0.10")

    // download
    implementation ("com.github.kittinunf.fuel:fuel:2.3.1")


    // test
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}
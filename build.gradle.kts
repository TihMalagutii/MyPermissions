plugins {
    id("java")
}

group = "com.tiagoh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly(files("C:/Users/tiago/Desktop/MyTale/hytale-downloader/2026.01.13-dcad8778f/HytaleServer.jar"))
}

tasks.test {
    useJUnitPlatform()
}
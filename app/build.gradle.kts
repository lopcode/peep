import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.15.3"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("peep.MainKt")
    tasks.run.get().workingDir = File(rootProject.projectDir, ".run")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    doNotTrackState("tests should always run")
    testLogging.showStandardStreams = true
    testLogging.events = TestLogEvent.values().toSet() - TestLogEvent.STARTED
}

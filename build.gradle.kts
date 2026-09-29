plugins {
    id("java")
    application
}

group = "io.veyralang"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "io.veyralang.VeyraCli"
    }
}

application {
    mainClass = "io.veyralang.VeyraCli"
}

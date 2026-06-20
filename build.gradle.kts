plugins {
    java
}

group = "net.meddle"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    // TAB has no published API artifact we consume; the jar is vendored in-repo.
    compileOnly(files("TAB v5.4.0.jar"))
}

tasks.processResources {
    // Mirror Maven's resource filtering: plugin.yml uses ${version}.
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

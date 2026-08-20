plugins {
    id("java")
    id("com.pigxity.portable-minecraft")
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

val minecraftVersion = providers.gradleProperty("minecraftVersion").get()
val projectMainClass = providers.gradleProperty("mainClass").get()

portableMinecraft {
    version.set(minecraftVersion)
    manifestTtlHours.set(24)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("portable-minecraft-$minecraftVersion")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest.attributes["Main-Class"] = projectMainClass
    from({
        configurations.runtimeClasspath.get().map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        }
    })
}

val runDirectory = layout.buildDirectory.dir("run")

tasks.register<JavaExec>("runClient") {
    dependsOn(tasks.build)

    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
    classpath(tasks.jar.flatMap { it.archiveFile })
    mainClass.set(projectMainClass)
    workingDir(runDirectory)

    doFirst {
        runDirectory.get().asFile.mkdirs()
    }
}

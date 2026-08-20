plugins {
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.1")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        create("portableMinecraft") {
            id = "com.pigxity.portable-minecraft"
            implementationClass = "com.pigxity.portablemc.build.plugin.PortableMinecraftPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

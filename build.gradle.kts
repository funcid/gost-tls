import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.8.21"
	application
}

group = "me.func"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

application {
	mainClass.set("ApplicationKt")
}
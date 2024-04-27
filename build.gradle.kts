import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.8.21"
	application
}

group = "me.func"
version = "1.0-SNAPSHOT"

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	implementation(files("./libs/ASN1P.jar"))
	implementation(files("./libs/AdES-core.jar"))
	implementation(files("./libs/CAdES.jar"))
	implementation(files("./libs/J6CF.jar"))
	implementation(files("./libs/J6Oscar.jar"))
	implementation(files("./libs/JCP.jar"))
	implementation(files("./libs/JCSP.jar"))
	implementation(files("./libs/JCPControlPane.jar"))
	implementation(files("./libs/JCPRequest.jar"))
	implementation(files("./libs/JCPRevCheck.jar"))
	implementation(files("./libs/JCPRevTools.jar"))
	implementation(files("./libs/JCPxml.jar"))
	implementation(files("./libs/JCryptoP.jar"))
	implementation(files("./libs/Rutoken.jar"))
	implementation(files("./libs/XAdES.jar"))
	implementation(files("./libs/XMLDSigRI.jar"))
	implementation(files("./libs/asn1rt.jar"))
	implementation(files("./libs/cmsutil.jar"))
	implementation(files("./libs/sspiSSL.jar"))
	implementation(files("./libs/cpSSL.jar"))
	implementation(files("./libs/tls_proxy.jar"))
	implementation(files("./libs/forms_rt.jar"))
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "1.8"
}

application {
	mainClass.set("MainKt")
}
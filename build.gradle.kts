val slf4jVersion: String by project

plugins {
  id("com.github.johnrengelman.shadow") version "5.2.0"
  kotlin("jvm").version("1.4.10")
  id("org.jlleitschuh.gradle.ktlint").version("10.0.0")
}

group = "example.asm"

repositories {
  mavenCentral()
  jcenter()
}
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.slf4j:slf4j-api:$slf4jVersion")
  implementation("org.slf4j:slf4j-simple:$slf4jVersion")

  implementation("org.ow2.asm:asm-commons:8.0.1")
  implementation("org.ow2.asm:asm-util:8.0.1")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = "11"
  }
  compileTestKotlin {
    kotlinOptions.jvmTarget = "11"
  }
}

ktlint {
  verbose.set(true)
  outputToConsole.set(true)
  coloredOutput.set(true)
//    reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}

tasks.shadowJar {
  archiveClassifier.set("")
  manifest {
    attributes(
      "Premain-Class" to "jagent.MethodInfoAgent",
      "Boot-Class-Path" to "asm-0.0.1.jar",
      "Implementation-Version" to project.version
    )
  }
}

tasks.named("build") {
  dependsOn("shadowJar")
}

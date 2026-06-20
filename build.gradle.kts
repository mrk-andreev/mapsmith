plugins {
  alias(libs.plugins.spotless)
  alias(libs.plugins.spotbugs) apply false
  alias(libs.plugins.errorprone) apply false
  alias(libs.plugins.maven.publish) apply false
}

group = "name.mrkandreev"

version =
    providers
        .gradleProperty("VERSION_NAME")
        .orElse(providers.environmentVariable("RELEASE_VERSION"))
        .orElse("1.0-SNAPSHOT")
        .get()

repositories { mavenCentral() }

spotless {
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt(libs.versions.ktfmt.get())
    trimTrailingWhitespace()
    endWithNewline()
  }
}

subprojects {
  group = rootProject.group
  version = rootProject.version

  repositories { mavenCentral() }

  plugins.withType<JavaPlugin> {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "jacoco")
    apply(plugin = "net.ltgt.errorprone")
    apply(plugin = "pmd")

    extensions.configure<JavaPluginExtension> {
      toolchain { languageVersion = JavaLanguageVersion.of(21) }
    }

    extensions.configure<JacocoPluginExtension> { toolVersion = libs.versions.jacoco.get() }

    extensions.configure<PmdExtension> {
      toolVersion = libs.versions.pmd.get()
      isConsoleOutput = true
    }

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
      java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
      }

      kotlinGradle {
        target("*.gradle.kts")
        ktfmt(libs.versions.ktfmt.get())
        trimTrailingWhitespace()
        endWithNewline()
      }
    }

    dependencies {
      add("errorprone", libs.errorprone.core)
      add("testImplementation", platform(libs.junit.bom))
      add("testImplementation", libs.junit.jupiter)
      add("testImplementation", libs.assertj.core)
      add("testRuntimeOnly", libs.junit.platform.launcher)
    }

    tasks.withType<JavaCompile>().configureEach {
      options.compilerArgs.add("-XDaddTypeAnnotationsToSymbol=true")
    }

    tasks.withType<Test>().configureEach {
      useJUnitPlatform()
      finalizedBy(tasks.named("jacocoTestReport"))
    }

    tasks.withType<JacocoReport>().configureEach {
      dependsOn(tasks.withType<Test>())

      reports {
        xml.required = true
        html.required = true
        csv.required = false
      }
    }

    tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
      excludeFilter = rootProject.layout.projectDirectory.file("config/spotbugs/exclude.xml")

      reports {
        create("html") { required = true }
        create("xml") { required = false }
      }
    }

    tasks.named("check") { dependsOn(tasks.named("jacocoTestReport")) }
  }
}

import net.ltgt.gradle.errorprone.errorprone
import org.gradle.external.javadoc.StandardJavadocDocletOptions

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
        // google-java-format only recognizes block tags at the start of a Javadoc line, so
        // normalize
        // hand-written Javadoc before formatting it.
        replaceRegex(
            "separate Javadoc block tags",
            "(?<!\\*) (?=@(?:author|deprecated|exception|param|return|see|serial|serialData|serialField|since|throws|version)\\b)",
            "\n * ",
        )
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
      add("testImplementation", libs.jqwik)
      add("testRuntimeOnly", libs.junit.platform.launcher)
    }

    tasks.withType<JavaCompile>().configureEach {
      options.compilerArgs.add("-Xlint:all")
      options.compilerArgs.add("-XDaddTypeAnnotationsToSymbol=true")
      options.errorprone.disable("ThreadPriorityCheck")
    }

    tasks.withType<Javadoc>().configureEach {
      (options as StandardJavadocDocletOptions).addBooleanOption("Werror", true)
      (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:all,-missing", true)
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

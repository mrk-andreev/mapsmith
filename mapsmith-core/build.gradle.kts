import java.math.BigDecimal
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification

plugins {
  `java-library`
  alias(libs.plugins.maven.publish)
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
  dependsOn(tasks.named("test"))
  executionData(layout.buildDirectory.file("jacoco/test.exec"))

  violationRules {
    rule {
      limit {
        counter = "LINE"
        value = "MISSEDCOUNT"
        maximum = BigDecimal.ZERO
      }
      limit {
        counter = "BRANCH"
        value = "MISSEDCOUNT"
        maximum = BigDecimal.ZERO
      }
    }
  }
}

tasks.named("check") { dependsOn(tasks.named("jacocoTestCoverageVerification")) }

mavenPublishing {
  coordinates(project.group.toString(), "mapsmith-core", project.version.toString())

  publishToMavenCentral(automaticRelease = true)
  signAllPublications()

  pom {
    name = "mapsmith-core"
    description = "High-performance primitive map implementations for Java."
    inceptionYear = "2026"
    url = "https://github.com/mrk-andreev/mapsmith"

    licenses {
      license {
        name = "MIT License"
        url = "https://opensource.org/license/mit/"
        distribution = "repo"
      }
    }

    developers {
      developer {
        id = "mrk-andreev"
        name = "Mark Andreev"
        url = "https://github.com/mrk-andreev"
      }
    }

    scm {
      url = "https://github.com/mrk-andreev/mapsmith"
      connection = "scm:git:https://github.com/mrk-andreev/mapsmith.git"
      developerConnection = "scm:git:ssh://git@github.com:mrk-andreev/mapsmith.git"
    }
  }
}

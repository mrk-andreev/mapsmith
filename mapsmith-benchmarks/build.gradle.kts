plugins { java }

val jmh by
    sourceSets.creating {
      java.srcDir("src/jmh/java")
      resources.srcDir("src/jmh/resources")
      compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
      runtimeClasspath += output + compileClasspath
    }

configurations[jmh.implementationConfigurationName].extendsFrom(
    configurations.testImplementation.get()
)

configurations[jmh.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
  implementation(project(":mapsmith-core"))

  add(jmh.implementationConfigurationName, project(":mapsmith-core"))
  add(jmh.implementationConfigurationName, libs.jmh.core)
  add(jmh.implementationConfigurationName, libs.picocli)
  add(jmh.annotationProcessorConfigurationName, libs.jmh.generator.annprocess)
}

tasks.register<JavaExec>("jmh") {
  group = "benchmark"
  description = "Runs the JMH benchmarks."
  dependsOn(tasks.named(jmh.classesTaskName))
  classpath = jmh.runtimeClasspath
  mainClass = "org.openjdk.jmh.Main"
}

tasks.named("check") { dependsOn(tasks.named(jmh.compileJavaTaskName)) }

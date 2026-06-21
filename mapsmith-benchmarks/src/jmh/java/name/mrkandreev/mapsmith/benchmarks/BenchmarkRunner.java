package name.mrkandreev.mapsmith.benchmarks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import picocli.CommandLine;

public enum BenchmarkRunner {
  ;

  public static void run(Class<?> cls, String[] args) throws RunnerException {
    var command = new RunBenchmarkCommand(cls);
    var commandLine = new CommandLine(command);
    var parseResult = commandLine.parseArgs(args);
    if (!CommandLine.printHelpIfRequested(parseResult)) {
      command.run();
    }
  }

  @CommandLine.Command(
      name = "benchmark",
      mixinStandardHelpOptions = true,
      description = "Runs a JMH benchmark and writes its results as JSON.")
  private static final class RunBenchmarkCommand {
    private final Class<?> benchmarkClass;

    @CommandLine.Option(
        names = {"-o", "--output-dir"},
        defaultValue = "build/benchmarks",
        paramLabel = "<directory>",
        description = "Directory for JSON results (default: ${DEFAULT-VALUE}).")
    private Path outputDirectory;

    @CommandLine.Option(
        names = {"-p", "--profiler"},
        paramLabel = "<name[:parameters]>",
        description =
            "JMH profiler, optionally followed by its parameters; may be specified more than once.")
    private final List<String> profilerSpecifications = new ArrayList<>();

    private RunBenchmarkCommand(Class<?> benchmarkClass) {
      this.benchmarkClass = benchmarkClass;
    }

    private void run() throws RunnerException {
      try {
        Files.createDirectories(outputDirectory);
      } catch (IOException exception) {
        throw new RunnerException(
            "Could not create benchmark output directory: " + outputDirectory, exception);
      }

      var optionsBuilder = new OptionsBuilder();
      optionsBuilder
          .include(benchmarkClass.getSimpleName())
          .result(outputDirectory.resolve(benchmarkClass.getSimpleName() + ".json").toString())
          .resultFormat(ResultFormatType.JSON);
      profilerSpecifications.forEach(specification -> addProfiler(optionsBuilder, specification));

      Options options = optionsBuilder.build();
      new Runner(options).run();
    }

    private static void addProfiler(OptionsBuilder optionsBuilder, String specification) {
      int parameterSeparator = specification.indexOf(':');
      if (parameterSeparator < 0) {
        optionsBuilder.addProfiler(specification);
      } else {
        optionsBuilder.addProfiler(
            specification.substring(0, parameterSeparator),
            specification.substring(parameterSeparator + 1));
      }
    }
  }
}

package name.mrkandreev.mapsmith.benchmarks;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import name.mrkandreev.mapsmith.range.LongObjectRangeMap;
import name.mrkandreev.mapsmith.range.TreeLongObjectRangeMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class LongObjectRangeMapBenchmark {
  private static final long RANGE_WIDTH = 16L;
  private static final long RANGE_STRIDE = 32L;

  @Benchmark
  public void putAll(Ranges ranges, Blackhole blackhole) {
    blackhole.consume(putAll(ranges));
  }

  @Benchmark
  public void putCoalescingAll(Ranges ranges, Blackhole blackhole) {
    blackhole.consume(putCoalescingAll(ranges));
  }

  @Benchmark
  public void getExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(getExisting(maps.map, maps.existingLookupKeys));
  }

  @Benchmark
  public void getMissing(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(getMissing(maps.map, maps.missingLookupKeys));
  }

  @Benchmark
  public void containsExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(containsExisting(maps.map, maps.existingLookupKeys));
  }

  @Benchmark
  public void removeAll(RemovableMaps maps, Blackhole blackhole) {
    blackhole.consume(removeAll(maps.map, maps.fromInclusive, maps.toInclusive));
  }

  private static LongObjectRangeMap<String> putAll(Ranges ranges) {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();
    for (int i = 0; i < ranges.fromInclusive.length; i++) {
      map.put(ranges.fromInclusive[i], ranges.toInclusive[i], ranges.mappedValues[i]);
    }
    return map;
  }

  private static LongObjectRangeMap<String> putCoalescingAll(Ranges ranges) {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();
    for (int i = 0; i < ranges.fromInclusive.length; i++) {
      map.putCoalescing(ranges.fromInclusive[i], ranges.toInclusive[i], ranges.coalescingValues[i]);
    }
    return map;
  }

  private static int getExisting(LongObjectRangeMap<String> map, long[] keys) {
    int result = 0;
    for (long key : keys) {
      result += map.get(key).length();
    }
    return result;
  }

  private static int getMissing(LongObjectRangeMap<String> map, long[] keys) {
    int result = 0;
    for (long key : keys) {
      result += map.getOrDefault(key, "missing").length();
    }
    return result;
  }

  private static int containsExisting(LongObjectRangeMap<String> map, long[] keys) {
    int result = 0;
    for (long key : keys) {
      if (map.containsKey(key)) {
        result++;
      }
    }
    return result;
  }

  private static LongObjectRangeMap<String> removeAll(
      LongObjectRangeMap<String> map, long[] from, long[] to) {
    for (int i = 0; i < from.length; i++) {
      map.remove(from[i], to[i]);
    }
    return map;
  }

  @State(Scope.Thread)
  public static class Ranges {
    @Param({"1000", "100000"})
    public int size;

    long[] fromInclusive;
    long[] toInclusive;
    String[] mappedValues;
    String[] coalescingValues;
    long[] existingLookupKeys;
    long[] missingLookupKeys;

    @Setup(Level.Trial)
    public void setUp() {
      fromInclusive = new long[size];
      toInclusive = new long[size];
      mappedValues = new String[size];
      coalescingValues = new String[size];
      existingLookupKeys = new long[size];
      missingLookupKeys = new long[size];

      fillRanges(
          fromInclusive,
          toInclusive,
          mappedValues,
          coalescingValues,
          existingLookupKeys,
          missingLookupKeys);
    }
  }

  @State(Scope.Thread)
  public static class PopulatedMaps extends Ranges {
    LongObjectRangeMap<String> map;

    @Setup(Level.Trial)
    @Override
    public void setUp() {
      super.setUp();

      map = putAll(this);
    }
  }

  @State(Scope.Thread)
  public static class RemovableMaps extends Ranges {
    LongObjectRangeMap<String> map;

    @Setup(Level.Invocation)
    @Override
    public void setUp() {
      super.setUp();

      map = putAll(this);
    }
  }

  private static void fillRanges(
      long[] fromInclusive,
      long[] toInclusive,
      String[] mappedValues,
      String[] coalescingValues,
      long[] existingLookupKeys,
      long[] missingLookupKeys) {
    SplittableRandom random = new SplittableRandom(0x72616e67656d6170L);
    for (int i = 0; i < fromInclusive.length; i++) {
      long from = i * RANGE_STRIDE;
      fromInclusive[i] = from;
      toInclusive[i] = from + RANGE_WIDTH - 1L;
      mappedValues[i] = "value-" + random.nextLong();
      coalescingValues[i] = "bucket-" + i / 10;
      existingLookupKeys[i] = from + random.nextLong(RANGE_WIDTH);
      missingLookupKeys[i] = from + RANGE_WIDTH;
    }
  }
}

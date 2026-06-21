package name.mrkandreev.mapsmith.benchmarks;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;
import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongObjectMapFactory;
import name.mrkandreev.mapsmith.openaddressing.MapSpecialization;
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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class LongObjectMapBenchmark {
  public static void main(String[] args) throws RunnerException {
    BenchmarkRunner.run(LongObjectMapBenchmark.class, args);
  }

  @Benchmark
  public void getExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(getExisting(maps.map, maps.lookupKeys));
  }

  @Benchmark
  public void putAll(Keys keys, Blackhole blackhole) {
    blackhole.consume(putAll(keys));
  }

  private static int getExisting(LongObjectMap<String> map, long[] lookupKeys) {
    int result = 0;
    for (long key : lookupKeys) {
      result += map.get(key).length();
    }
    return result;
  }

  private static LongObjectMap<String> putAll(Keys keys) {
    LongObjectMap<String> map = keys.mapKind.newMap(keys.size);
    for (int i = 0; i < keys.lookupKeys.length; i++) {
      map.put(keys.lookupKeys[i], keys.mappedValues[i]);
    }
    return map;
  }

  @State(Scope.Thread)
  public static class Keys {
    @Param({
      "LINEAR_PROBING_MURMUR3_FINALIZER",
      "LINEAR_PROBING_FIBONACCI",
      "LINEAR_PROBING_XOR_SHIFT",
      "LINEAR_PROBING_IDENTITY",
      "ROBIN_HOOD_MURMUR3_FINALIZER",
      "ROBIN_HOOD_FIBONACCI",
      "ROBIN_HOOD_XOR_SHIFT",
      "ROBIN_HOOD_IDENTITY",
      "SWISS_TABLE_MURMUR3_FINALIZER",
      "SWISS_TABLE_FIBONACCI",
      "SWISS_TABLE_XOR_SHIFT",
      "SWISS_TABLE_IDENTITY",
      "HASH_MAP"
    })
    public MapKind mapKind;

    @Param({"1000", "100000"})
    public int size;

    long[] lookupKeys;
    String[] mappedValues;

    @Setup(Level.Trial)
    public void setUp() {
      lookupKeys = new long[size];
      mappedValues = new String[size];

      fillKeys(lookupKeys, mappedValues);
    }
  }

  public enum MapKind {
    LINEAR_PROBING_MURMUR3_FINALIZER(
        MapSpecialization.LINEAR_PROBING, LongHashing.MURMUR3_FINALIZER),
    LINEAR_PROBING_FIBONACCI(MapSpecialization.LINEAR_PROBING, LongHashing.FIBONACCI),
    LINEAR_PROBING_XOR_SHIFT(MapSpecialization.LINEAR_PROBING, LongHashing.XOR_SHIFT),
    LINEAR_PROBING_IDENTITY(MapSpecialization.LINEAR_PROBING, LongHashing.IDENTITY),
    ROBIN_HOOD_MURMUR3_FINALIZER(MapSpecialization.ROBIN_HOOD, LongHashing.MURMUR3_FINALIZER),
    ROBIN_HOOD_FIBONACCI(MapSpecialization.ROBIN_HOOD, LongHashing.FIBONACCI),
    ROBIN_HOOD_XOR_SHIFT(MapSpecialization.ROBIN_HOOD, LongHashing.XOR_SHIFT),
    ROBIN_HOOD_IDENTITY(MapSpecialization.ROBIN_HOOD, LongHashing.IDENTITY),
    SWISS_TABLE_MURMUR3_FINALIZER(MapSpecialization.SWISS_TABLE, LongHashing.MURMUR3_FINALIZER),
    SWISS_TABLE_FIBONACCI(MapSpecialization.SWISS_TABLE, LongHashing.FIBONACCI),
    SWISS_TABLE_XOR_SHIFT(MapSpecialization.SWISS_TABLE, LongHashing.XOR_SHIFT),
    SWISS_TABLE_IDENTITY(MapSpecialization.SWISS_TABLE, LongHashing.IDENTITY),
    HASH_MAP(null, null);

    private final MapSpecialization mapSpecialization;
    private final LongHashing hashing;

    MapKind(MapSpecialization specialization, LongHashing hashing) {
      mapSpecialization = specialization;
      this.hashing = hashing;
    }

    private LongObjectMap<String> newMap(int expectedSize) {
      if (this == HASH_MAP) {
        return new HashMapLongObjectMap<>(expectedSize);
      }
      return LongObjectMapFactory.create(mapSpecialization, expectedSize, hashing);
    }
  }

  @State(Scope.Thread)
  public static class PopulatedMaps extends Keys {
    LongObjectMap<String> map;

    @Setup(Level.Trial)
    @Override
    public void setUp() {
      super.setUp();

      map = mapKind.newMap(size);

      for (int i = 0; i < lookupKeys.length; i++) {
        map.put(lookupKeys[i], mappedValues[i]);
      }
    }
  }

  private record HashMapLongObjectMap<T>(Map<Long, T> delegate) implements LongObjectMap<T> {
    private HashMapLongObjectMap(int expectedSize) {
      this(new HashMap<>(hashMapCapacity(expectedSize)));
    }

    @Override
    public int size() {
      return delegate.size();
    }

    @Override
    public boolean containsKey(long key) {
      return delegate.containsKey(key);
    }

    @Override
    public T get(long key) {
      return delegate.get(key);
    }

    @Override
    public T getOrDefault(long key, T defaultValue) {
      return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public T put(long key, T value) {
      return delegate.put(key, value);
    }

    @Override
    public T remove(long key) {
      return delegate.remove(key);
    }

    @Override
    public void clear() {
      delegate.clear();
    }

    private static int hashMapCapacity(int size) {
      return (int) (size / 0.75f) + 1;
    }
  }

  private static void fillKeys(long[] lookupKeys, String[] mappedValues) {
    SplittableRandom random = new SplittableRandom(0x4d6170736d697468L);
    for (int i = 0; i < lookupKeys.length; i++) {
      lookupKeys[i] = random.nextLong();
      mappedValues[i] = "value-" + i;
    }
  }
}

package name.mrkandreev.mapsmith.benchmarks;

import java.util.Comparator;
import java.util.SplittableRandom;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import name.mrkandreev.mapsmith.ranking.LongLongRankingMap;
import name.mrkandreev.mapsmith.ranking.OrderStatisticLongLongMap;
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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class LongLongRankingMapBenchmark {
  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder().include(LongLongRankingMapBenchmark.class.getSimpleName()).build();
    new Runner(options).run();
  }

  @Benchmark
  public void putAll(Keys keys, Blackhole blackhole) {
    blackhole.consume(putAll(keys));
  }

  @Benchmark
  public void putExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(putExisting(maps.map, maps.lookupKeys, maps.updatedValues));
  }

  @Benchmark
  public void rankOfExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(rankOfExisting(maps.map, maps.lookupKeys));
  }

  @Benchmark
  public void countBeforeExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(countBeforeExisting(maps.map, maps.lookupKeys));
  }

  @Benchmark
  public void countAfterExisting(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(countAfterExisting(maps.map, maps.lookupKeys));
  }

  @Benchmark
  public void removeAll(PopulatedMaps maps, Blackhole blackhole) {
    blackhole.consume(removeAll(maps.map, maps.lookupKeys));
  }

  private static LongLongRankingMap putAll(Keys keys) {
    LongLongRankingMap map = keys.mapKind.newMap(keys.size);
    for (int i = 0; i < keys.lookupKeys.length; i++) {
      map.put(keys.lookupKeys[i], keys.mappedValues[i]);
    }
    return map;
  }

  private static long putExisting(LongLongRankingMap map, long[] keys, long[] values) {
    long result = 0L;
    for (int i = 0; i < keys.length; i++) {
      result += map.put(keys[i], values[i]);
    }
    return result;
  }

  private static long rankOfExisting(LongLongRankingMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.rankOf(key);
    }
    return result;
  }

  private static long countBeforeExisting(LongLongRankingMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.countBefore(key);
    }
    return result;
  }

  private static long countAfterExisting(LongLongRankingMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.countAfter(key);
    }
    return result;
  }

  private static long removeAll(LongLongRankingMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.remove(key);
    }
    return result;
  }

  @State(Scope.Thread)
  public static class Keys {
    @Param({"ORDER_STATISTIC", "TREE_MAP"})
    public MapKind mapKind;

    @Param({"1000", "100000"})
    public int size;

    long[] lookupKeys;
    long[] mappedValues;
    long[] updatedValues;

    @Setup(Level.Trial)
    public void setUp() {
      lookupKeys = new long[size];
      mappedValues = new long[size];
      updatedValues = new long[size];

      fillKeys(lookupKeys, mappedValues, updatedValues);
    }
  }

  public enum MapKind {
    ORDER_STATISTIC {
      @Override
      LongLongRankingMap newMap(int expectedSize) {
        return new OrderStatisticLongLongMap(expectedSize);
      }
    },
    TREE_MAP {
      @Override
      LongLongRankingMap newMap(int expectedSize) {
        return new TreeLongLongRankingMap();
      }
    };

    abstract LongLongRankingMap newMap(int expectedSize);
  }

  @State(Scope.Thread)
  public static class PopulatedMaps extends Keys {
    LongLongRankingMap map;

    @Setup(Level.Invocation)
    @Override
    public void setUp() {
      super.setUp();

      map = mapKind.newMap(size);
      for (int i = 0; i < lookupKeys.length; i++) {
        map.put(lookupKeys[i], mappedValues[i]);
      }
    }
  }

  private static final class TreeLongLongRankingMap implements LongLongRankingMap {
    private final TreeMap<Long, Long> valueByKey = new TreeMap<>();
    private final TreeMap<RankKey, Long> keyByRank =
        new TreeMap<>(
            Comparator.comparingLong(RankKey::value).reversed().thenComparingLong(RankKey::key));

    @Override
    public int size() {
      return valueByKey.size();
    }

    @Override
    public boolean containsKey(long key) {
      return valueByKey.containsKey(key);
    }

    @Override
    public long get(long key) {
      return getOrDefault(key, 0L);
    }

    @Override
    public long getOrDefault(long key, long defaultValue) {
      return valueByKey.getOrDefault(key, defaultValue);
    }

    @Override
    public long put(long key, long value) {
      Long previousValue = valueByKey.put(key, value);
      if (previousValue != null) {
        keyByRank.remove(new RankKey(key, previousValue));
      }
      keyByRank.put(new RankKey(key, value), key);
      return previousValue == null ? 0L : previousValue;
    }

    @Override
    public long remove(long key) {
      Long previousValue = valueByKey.remove(key);
      if (previousValue == null) {
        return 0L;
      }
      keyByRank.remove(new RankKey(key, previousValue));
      return previousValue;
    }

    @Override
    public void clear() {
      valueByKey.clear();
      keyByRank.clear();
    }

    @Override
    public int rankOf(long key) {
      int countBefore = countBefore(key);
      return countBefore == MISSING_RANK ? MISSING_RANK : countBefore + 1;
    }

    @Override
    public int countBefore(long key) {
      Long value = valueByKey.get(key);
      if (value == null) {
        return MISSING_RANK;
      }

      int count = 0;
      RankKey requested = new RankKey(key, value);
      for (RankKey current : keyByRank.keySet()) {
        if (current.equals(requested)) {
          return count;
        }
        count++;
      }
      return MISSING_RANK;
    }

    @Override
    public int countAfter(long key) {
      int countBefore = countBefore(key);
      return countBefore == MISSING_RANK ? MISSING_RANK : size() - countBefore - 1;
    }
  }

  private record RankKey(long key, long value) {}

  private static void fillKeys(long[] lookupKeys, long[] mappedValues, long[] updatedValues) {
    SplittableRandom random = new SplittableRandom(0x72616e6b696e6773L);
    for (int i = 0; i < lookupKeys.length; i++) {
      lookupKeys[i] = random.nextLong();
      mappedValues[i] = random.nextLong();
      updatedValues[i] = random.nextLong();
    }
  }
}

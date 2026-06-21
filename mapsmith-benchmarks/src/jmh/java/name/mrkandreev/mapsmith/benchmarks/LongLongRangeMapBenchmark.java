package name.mrkandreev.mapsmith.benchmarks;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.SplittableRandom;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import name.mrkandreev.mapsmith.range.LongBoundType;
import name.mrkandreev.mapsmith.range.LongLongRangeMap;
import name.mrkandreev.mapsmith.range.TreeLongLongRangeMap;
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
import org.openjdk.jmh.runner.RunnerException;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
public class LongLongRangeMapBenchmark {
  public static void main(String[] args) throws RunnerException {
    BenchmarkRunner.run(LongLongRangeMapBenchmark.class, args);
  }

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

  private static LongLongRangeMap putAll(Ranges ranges) {
    LongLongRangeMap map = ranges.mapKind.newMap();
    for (int i = 0; i < ranges.fromInclusive.length; i++) {
      map.put(ranges.fromInclusive[i], ranges.toInclusive[i], ranges.mappedValues[i]);
    }
    return map;
  }

  private static LongLongRangeMap putCoalescingAll(Ranges ranges) {
    LongLongRangeMap map = ranges.mapKind.newMap();
    for (int i = 0; i < ranges.fromInclusive.length; i++) {
      map.putCoalescing(ranges.fromInclusive[i], ranges.toInclusive[i], ranges.coalescingValues[i]);
    }
    return map;
  }

  private static long getExisting(LongLongRangeMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.get(key);
    }
    return result;
  }

  private static long getMissing(LongLongRangeMap map, long[] keys) {
    long result = 0L;
    for (long key : keys) {
      result += map.getOrDefault(key, -1L);
    }
    return result;
  }

  private static int containsExisting(LongLongRangeMap map, long[] keys) {
    int result = 0;
    for (long key : keys) {
      if (map.containsKey(key)) {
        result++;
      }
    }
    return result;
  }

  private static LongLongRangeMap removeAll(LongLongRangeMap map, long[] from, long[] to) {
    for (int i = 0; i < from.length; i++) {
      map.remove(from[i], to[i]);
    }
    return map;
  }

  @State(Scope.Thread)
  public static class Ranges {
    @Param({"RANGE_TREE_MAP", "RANGE_KEY_TREE_MAP"})
    public MapKind mapKind;

    @Param({"1000", "100000"})
    public int size;

    long[] fromInclusive;
    long[] toInclusive;
    long[] mappedValues;
    long[] coalescingValues;
    long[] existingLookupKeys;
    long[] missingLookupKeys;

    @Setup(Level.Trial)
    public void setUp() {
      fromInclusive = new long[size];
      toInclusive = new long[size];
      mappedValues = new long[size];
      coalescingValues = new long[size];
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

  public enum MapKind {
    RANGE_TREE_MAP {
      @Override
      LongLongRangeMap newMap() {
        return new TreeLongLongRangeMap();
      }
    },
    RANGE_KEY_TREE_MAP {
      @Override
      LongLongRangeMap newMap() {
        return new RangeKeyTreeLongLongRangeMap();
      }
    };

    abstract LongLongRangeMap newMap();
  }

  @State(Scope.Thread)
  public static class PopulatedMaps extends Ranges {
    LongLongRangeMap map;

    @Setup(Level.Trial)
    @Override
    public void setUp() {
      super.setUp();

      map = putAll(this);
    }
  }

  @State(Scope.Thread)
  public static class RemovableMaps extends Ranges {
    LongLongRangeMap map;

    @Setup(Level.Invocation)
    @Override
    public void setUp() {
      super.setUp();

      map = putAll(this);
    }
  }

  private static final class RangeKeyTreeLongLongRangeMap implements LongLongRangeMap {
    private final NavigableMap<RangeKey, Long> ranges =
        new TreeMap<>(
            Comparator.comparingLong(RangeKey::fromInclusive)
                .thenComparingLong(RangeKey::toInclusive));

    @Override
    public int size() {
      return ranges.size();
    }

    @Override
    public boolean containsKey(long key) {
      return rangeFor(key) != null;
    }

    @Override
    public long get(long key) {
      return getOrDefault(key, 0L);
    }

    @Override
    public long getOrDefault(long key, long defaultValue) {
      var range = rangeFor(key);
      return range == null ? defaultValue : range.getValue();
    }

    @Override
    public void put(long fromInclusive, long toInclusive, long value) {
      validateClosedRange(fromInclusive, toInclusive);
      putClosed(fromInclusive, toInclusive, value, false);
    }

    @Override
    public void put(
        long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value) {
      Bounds bounds = toClosedBounds(lower, lowerType, upper, upperType);
      if (!bounds.isEmpty()) {
        putClosed(bounds.fromInclusive(), bounds.toInclusive(), value, false);
      }
    }

    @Override
    public void putCoalescing(long fromInclusive, long toInclusive, long value) {
      validateClosedRange(fromInclusive, toInclusive);
      putClosed(fromInclusive, toInclusive, value, true);
    }

    @Override
    public void putCoalescing(
        long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value) {
      Bounds bounds = toClosedBounds(lower, lowerType, upper, upperType);
      if (!bounds.isEmpty()) {
        putClosed(bounds.fromInclusive(), bounds.toInclusive(), value, true);
      }
    }

    @Override
    public void remove(long fromInclusive, long toInclusive) {
      validateClosedRange(fromInclusive, toInclusive);
      removeClosed(fromInclusive, toInclusive);
    }

    @Override
    public void remove(long lower, LongBoundType lowerType, long upper, LongBoundType upperType) {
      Bounds bounds = toClosedBounds(lower, lowerType, upper, upperType);
      if (!bounds.isEmpty()) {
        removeClosed(bounds.fromInclusive(), bounds.toInclusive());
      }
    }

    @Override
    public void clear() {
      ranges.clear();
    }

    private void putClosed(long fromInclusive, long toInclusive, long value, boolean coalesce) {
      removeClosed(fromInclusive, toInclusive);
      RangeKey range = new RangeKey(fromInclusive, toInclusive);
      if (coalesce) {
        range = mergePrevious(range, value);
        range = mergeNext(range, value);
      }
      ranges.put(range, value);
    }

    private void removeClosed(long fromInclusive, long toInclusive) {
      var current = ranges.floorEntry(RangeKey.atOrBefore(fromInclusive));
      if (current == null || current.getKey().toInclusive() < fromInclusive) {
        current = ranges.ceilingEntry(RangeKey.atOrAfter(fromInclusive));
      }

      while (current != null) {
        RangeKey range = current.getKey();
        long value = current.getValue();
        if (range.fromInclusive() > toInclusive) {
          return;
        }

        var next = ranges.higherEntry(range);
        ranges.remove(range);

        if (range.fromInclusive() < fromInclusive) {
          ranges.put(new RangeKey(range.fromInclusive(), fromInclusive - 1L), value);
        }
        if (range.toInclusive() > toInclusive) {
          ranges.put(new RangeKey(toInclusive + 1L, range.toInclusive()), value);
          return;
        }

        current = next;
      }
    }

    private java.util.Map.Entry<RangeKey, Long> rangeFor(long key) {
      var entry = ranges.floorEntry(RangeKey.atOrBefore(key));
      if (entry == null || entry.getKey().toInclusive() < key) {
        return null;
      }
      return entry;
    }

    private RangeKey mergePrevious(RangeKey range, long value) {
      var previous = ranges.lowerEntry(range);
      if (previous == null
          || previous.getValue() != value
          || !touches(previous.getKey().toInclusive(), range.fromInclusive())) {
        return range;
      }

      ranges.remove(previous.getKey());
      return new RangeKey(previous.getKey().fromInclusive(), range.toInclusive());
    }

    private RangeKey mergeNext(RangeKey range, long value) {
      RangeKey result = range;
      var next = ranges.ceilingEntry(result);
      while (next != null
          && next.getValue() == value
          && touches(result.toInclusive(), next.getKey().fromInclusive())) {
        ranges.remove(next.getKey());
        result = new RangeKey(result.fromInclusive(), next.getKey().toInclusive());
        next = ranges.ceilingEntry(result);
      }
      return result;
    }
  }

  private record RangeKey(long fromInclusive, long toInclusive) {
    private static RangeKey atOrBefore(long key) {
      return new RangeKey(key, Long.MAX_VALUE);
    }

    private static RangeKey atOrAfter(long key) {
      return new RangeKey(key, Long.MIN_VALUE);
    }
  }

  private record Bounds(long fromInclusive, long toInclusive) {
    private boolean isEmpty() {
      return fromInclusive > toInclusive;
    }
  }

  private static Bounds toClosedBounds(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType) {
    if (lowerType == null) {
      throw new NullPointerException("lowerType must not be null");
    }
    if (upperType == null) {
      throw new NullPointerException("upperType must not be null");
    }
    if (lower > upper) {
      throw new IllegalArgumentException("lower must be <= upper");
    }
    if (lower == upper && lowerType == LongBoundType.OPEN && upperType == LongBoundType.OPEN) {
      throw new IllegalArgumentException("open range endpoints must be different");
    }

    long fromInclusive = lower;
    long toInclusive = upper;
    if (lowerType == LongBoundType.OPEN) {
      if (lower == Long.MAX_VALUE) {
        return new Bounds(1L, 0L);
      }
      fromInclusive++;
    }
    if (upperType == LongBoundType.OPEN) {
      if (upper == Long.MIN_VALUE) {
        return new Bounds(1L, 0L);
      }
      toInclusive--;
    }
    return new Bounds(fromInclusive, toInclusive);
  }

  private static void validateClosedRange(long fromInclusive, long toInclusive) {
    if (fromInclusive > toInclusive) {
      throw new IllegalArgumentException("fromInclusive must be <= toInclusive");
    }
  }

  private static boolean touches(long leftToInclusive, long rightFromInclusive) {
    return leftToInclusive == Long.MAX_VALUE || leftToInclusive + 1L >= rightFromInclusive;
  }

  private static void fillRanges(
      long[] fromInclusive,
      long[] toInclusive,
      long[] mappedValues,
      long[] coalescingValues,
      long[] existingLookupKeys,
      long[] missingLookupKeys) {
    SplittableRandom random = new SplittableRandom(0x72616e67656d6170L);
    for (int i = 0; i < fromInclusive.length; i++) {
      long from = i * RANGE_STRIDE;
      fromInclusive[i] = from;
      toInclusive[i] = from + RANGE_WIDTH - 1L;
      mappedValues[i] = random.nextLong();
      coalescingValues[i] = i / 10L;
      existingLookupKeys[i] = from + random.nextLong(RANGE_WIDTH);
      missingLookupKeys[i] = from + RANGE_WIDTH;
    }
  }
}

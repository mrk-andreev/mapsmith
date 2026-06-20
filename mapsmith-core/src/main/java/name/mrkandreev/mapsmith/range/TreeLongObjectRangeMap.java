package name.mrkandreev.mapsmith.range;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class TreeLongObjectRangeMap<T> implements LongObjectRangeMap<T> {
  private final TreeMap<Long, Range<T>> ranges = new TreeMap<>();

  @Override
  public int size() {
    return ranges.size();
  }

  @Override
  public boolean containsKey(long key) {
    return rangeFor(key) != null;
  }

  @Override
  public T get(long key) {
    return getOrDefault(key, null);
  }

  @Override
  public T getOrDefault(long key, T defaultValue) {
    Range<T> range = rangeFor(key);
    return range == null ? defaultValue : range.value;
  }

  @Override
  public void put(long fromInclusive, long toInclusive, T value) {
    validateClosedRange(fromInclusive, toInclusive);
    putClosed(fromInclusive, toInclusive, value, false);
  }

  @Override
  public void put(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType, T value) {
    putCanonical(lower, lowerType, upper, upperType, value, false);
  }

  @Override
  public void putCoalescing(long fromInclusive, long toInclusive, T value) {
    validateClosedRange(fromInclusive, toInclusive);
    putClosed(fromInclusive, toInclusive, value, true);
  }

  @Override
  public void putCoalescing(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType, T value) {
    putCanonical(lower, lowerType, upper, upperType, value, true);
  }

  @Override
  public void remove(long fromInclusive, long toInclusive) {
    validateClosedRange(fromInclusive, toInclusive);
    removeClosed(fromInclusive, toInclusive);
  }

  @Override
  public void remove(long lower, LongBoundType lowerType, long upper, LongBoundType upperType) {
    removeCanonical(lower, lowerType, upper, upperType);
  }

  private void removeClosed(long fromInclusive, long toInclusive) {
    Map.Entry<Long, Range<T>> current = ranges.floorEntry(fromInclusive);
    if (current == null || current.getValue().to < fromInclusive) {
      current = ranges.ceilingEntry(fromInclusive);
    }

    while (current != null) {
      Range<T> range = current.getValue();
      if (range.from > toInclusive) {
        return;
      }

      Map.Entry<Long, Range<T>> next = ranges.higherEntry(range.from);
      ranges.remove(range.from);

      if (range.from < fromInclusive) {
        ranges.put(range.from, new Range<>(range.from, fromInclusive - 1L, range.value));
      }
      if (range.to > toInclusive) {
        ranges.put(toInclusive + 1L, new Range<>(toInclusive + 1L, range.to, range.value));
        return;
      }

      current = next;
    }
  }

  @Override
  public void clear() {
    ranges.clear();
  }

  private Range<T> rangeFor(long key) {
    Map.Entry<Long, Range<T>> entry = ranges.floorEntry(key);
    if (entry == null) {
      return null;
    }

    Range<T> range = entry.getValue();
    return range.to >= key ? range : null;
  }

  private void putClosed(long fromInclusive, long toInclusive, T value, boolean coalesce) {
    removeClosed(fromInclusive, toInclusive);
    Range<T> storedRange = new Range<>(fromInclusive, toInclusive, value);
    if (coalesce) {
      putCoalescing(storedRange);
    } else {
      ranges.put(storedRange.from, storedRange);
    }
  }

  private void putCanonical(
      long lower,
      LongBoundType lowerType,
      long upper,
      LongBoundType upperType,
      T value,
      boolean coalesce) {
    Objects.requireNonNull(lowerType, "lowerType must not be null");
    Objects.requireNonNull(upperType, "upperType must not be null");
    validateTypedRange(lower, lowerType, upper, upperType);

    long fromInclusive = lower;
    long toInclusive = upper;
    if (lowerType == LongBoundType.OPEN) {
      if (lower == Long.MAX_VALUE) {
        return;
      }
      fromInclusive++;
    }
    if (upperType == LongBoundType.OPEN) {
      if (upper == Long.MIN_VALUE) {
        return;
      }
      toInclusive--;
    }

    if (fromInclusive <= toInclusive) {
      putClosed(fromInclusive, toInclusive, value, coalesce);
    }
  }

  private void removeCanonical(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType) {
    Objects.requireNonNull(lowerType, "lowerType must not be null");
    Objects.requireNonNull(upperType, "upperType must not be null");
    validateTypedRange(lower, lowerType, upper, upperType);

    long fromInclusive = lower;
    long toInclusive = upper;
    if (lowerType == LongBoundType.OPEN) {
      if (lower == Long.MAX_VALUE) {
        return;
      }
      fromInclusive++;
    }
    if (upperType == LongBoundType.OPEN) {
      if (upper == Long.MIN_VALUE) {
        return;
      }
      toInclusive--;
    }

    if (fromInclusive <= toInclusive) {
      removeClosed(fromInclusive, toInclusive);
    }
  }

  private void putCoalescing(Range<T> range) {
    Range<T> merged = mergePrevious(range);
    merged = mergeNext(merged);
    ranges.put(merged.from, merged);
  }

  private Range<T> mergePrevious(Range<T> range) {
    Map.Entry<Long, Range<T>> previousEntry = ranges.lowerEntry(range.from);
    if (previousEntry == null) {
      return range;
    }

    Range<T> previous = previousEntry.getValue();
    if (!Objects.equals(previous.value, range.value) || !touches(previous.to, range.from)) {
      return range;
    }

    ranges.remove(previous.from);
    return new Range<>(previous.from, range.to, range.value);
  }

  private Range<T> mergeNext(Range<T> range) {
    Range<T> result = range;
    Map.Entry<Long, Range<T>> nextEntry = ranges.ceilingEntry(result.from);
    while (nextEntry != null) {
      Range<T> next = nextEntry.getValue();
      if (!Objects.equals(next.value, result.value) || !touches(result.to, next.from)) {
        return result;
      }

      ranges.remove(next.from);
      result = new Range<>(result.from, next.to, result.value);
      nextEntry = ranges.ceilingEntry(result.from);
    }
    return result;
  }

  private static boolean touches(long leftTo, long rightFrom) {
    return leftTo == Long.MAX_VALUE || leftTo + 1L >= rightFrom;
  }

  private static void validateTypedRange(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType) {
    if (lower > upper) {
      throw new IllegalArgumentException("lower must be <= upper");
    }
    if (lower == upper && lowerType == LongBoundType.OPEN && upperType == LongBoundType.OPEN) {
      throw new IllegalArgumentException("open range endpoints must be different");
    }
  }

  private static void validateClosedRange(long fromInclusive, long toInclusive) {
    if (fromInclusive > toInclusive) {
      throw new IllegalArgumentException("fromInclusive must be <= toInclusive");
    }
  }

  private record Range<T>(long from, long to, T value) {}
}

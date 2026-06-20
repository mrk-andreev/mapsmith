package name.mrkandreev.mapsmith.range;

public interface LongLongRangeMap {
  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  boolean containsKey(long key);

  long get(long key);

  long getOrDefault(long key, long defaultValue);

  void put(long fromInclusive, long toInclusive, long value);

  void put(long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value);

  void putCoalescing(long fromInclusive, long toInclusive, long value);

  void putCoalescing(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value);

  void remove(long fromInclusive, long toInclusive);

  void remove(long lower, LongBoundType lowerType, long upper, LongBoundType upperType);

  void clear();
}

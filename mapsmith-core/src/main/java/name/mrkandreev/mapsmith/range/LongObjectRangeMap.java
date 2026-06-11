package name.mrkandreev.mapsmith.range;

public interface LongObjectRangeMap<T> {
  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  boolean containsKey(long key);

  T get(long key);

  T getOrDefault(long key, T defaultValue);

  void put(long fromInclusive, long toInclusive, T value);

  void put(long lower, LongBoundType lowerType, long upper, LongBoundType upperType, T value);

  void putCoalescing(long fromInclusive, long toInclusive, T value);

  void putCoalescing(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType, T value);

  void remove(long fromInclusive, long toInclusive);

  void remove(long lower, LongBoundType lowerType, long upper, LongBoundType upperType);

  void clear();
}

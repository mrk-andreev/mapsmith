package name.mrkandreev.mapsmith.range;

/** A map from inclusive {@code long} ranges to primitive {@code long} values. */
public interface LongLongRangeMap {
  /** Returns the number of ranges. */
  int size();

  /** Returns whether this map has no ranges. */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns whether {@code key} is covered.
   *
   * @param key key to look up
   * @return whether covered
   */
  boolean containsKey(long key);

  /**
   * Returns the value covering {@code key}.
   *
   * @param key key to look up
   * @return mapped value
   */
  long get(long key);

  /**
   * Returns the value covering {@code key}, or {@code defaultValue}.
   *
   * @param key key to look up
   * @param defaultValue value returned when absent
   * @return mapped or default value
   */
  long getOrDefault(long key, long defaultValue);

  /**
   * Associates {@code value} with an inclusive range.
   *
   * @param fromInclusive lower bound
   * @param toInclusive upper bound
   * @param value value to store
   */
  void put(long fromInclusive, long toInclusive, long value);

  /**
   * Associates {@code value} with a bounded range.
   *
   * @param lower lower bound
   * @param lowerType lower-bound type
   * @param upper upper bound
   * @param upperType upper-bound type
   * @param value value to store
   */
  void put(long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value);

  /**
   * Associates {@code value} with an inclusive range and coalesces adjacent ranges.
   *
   * @param fromInclusive lower bound
   * @param toInclusive upper bound
   * @param value value to store
   */
  void putCoalescing(long fromInclusive, long toInclusive, long value);

  /**
   * Associates {@code value} with a bounded range and coalesces adjacent ranges.
   *
   * @param lower lower bound
   * @param lowerType lower-bound type
   * @param upper upper bound
   * @param upperType upper-bound type
   * @param value value to store
   */
  void putCoalescing(
      long lower, LongBoundType lowerType, long upper, LongBoundType upperType, long value);

  /**
   * Removes an inclusive range.
   *
   * @param fromInclusive lower bound
   * @param toInclusive upper bound
   */
  void remove(long fromInclusive, long toInclusive);

  /**
   * Removes a bounded range.
   *
   * @param lower lower bound
   * @param lowerType lower-bound type
   * @param upper upper bound
   * @param upperType upper-bound type
   */
  void remove(long lower, LongBoundType lowerType, long upper, LongBoundType upperType);

  /** Removes all ranges. */
  void clear();
}

package name.mrkandreev.mapsmith;

/** A map from primitive {@code long} keys to primitive {@code long} values. */
public interface LongLongMap {
  /**
   * Returns the number of entries.
   *
   * @return entry count
   */
  int size();

  /**
   * Returns whether this map has no entries.
   *
   * @return whether empty
   */
  default boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns whether {@code key} is present.
   *
   * @param key key to look up
   * @return whether present
   */
  boolean containsKey(long key);

  /**
   * Returns the value for {@code key}.
   *
   * @param key key to look up
   * @return mapped value
   */
  long get(long key);

  /**
   * Returns the value for {@code key}, or {@code defaultValue}.
   *
   * @param key key to look up
   * @param defaultValue value returned when absent
   * @return mapped or default value
   */
  long getOrDefault(long key, long defaultValue);

  /**
   * Associates {@code value} with {@code key}.
   *
   * @param key key to store
   * @param value value to store
   * @return previous value
   */
  long put(long key, long value);

  /**
   * Removes {@code key}.
   *
   * @param key key to remove
   * @return previous value
   */
  long remove(long key);

  /** Removes all entries. */
  void clear();
}

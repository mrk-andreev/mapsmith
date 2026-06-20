package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;

/** A configurable primitive long-to-object open-addressing map. */
public final class LongObjectOpenAddressMap<T> implements LongObjectMap<T> {
  /** Default expected entry count. */
  public static final int DEFAULT_EXPECTED_SIZE = 16;

  private final LongObjectMap<T> delegate;

  /** Creates a map with the default strategy and capacity. */
  public LongObjectOpenAddressMap() {
    this(defaultStrategy());
  }

  /**
   * Creates a map with the default strategy.
   *
   * @param expectedSize expected entry count
   */
  public LongObjectOpenAddressMap(int expectedSize) {
    this(defaultStrategy(), expectedSize);
  }

  /**
   * Creates a map.
   *
   * @param strategy strategy to use
   */
  public LongObjectOpenAddressMap(LongObjectOpenAddressingStrategy<T> strategy) {
    this(strategy, DEFAULT_EXPECTED_SIZE);
  }

  /**
   * Creates a map.
   *
   * @param strategy strategy to use
   * @param expectedSize expected entry count
   */
  public LongObjectOpenAddressMap(LongObjectOpenAddressingStrategy<T> strategy, int expectedSize) {
    this(strategy, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  /**
   * Creates a map.
   *
   * @param strategy strategy to use
   * @param hashing hash function
   */
  public LongObjectOpenAddressMap(
      LongObjectOpenAddressingStrategy<T> strategy, LongHashing hashing) {
    this(strategy, DEFAULT_EXPECTED_SIZE, hashing);
  }

  /**
   * Creates a map.
   *
   * @param strategy strategy to use
   * @param expectedSize expected entry count
   * @param hashing hash function
   */
  public LongObjectOpenAddressMap(
      LongObjectOpenAddressingStrategy<T> strategy, int expectedSize, LongHashing hashing) {
    Objects.requireNonNull(strategy, "strategy must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");
    delegate =
        Objects.requireNonNull(
            strategy.create(expectedSize, hashing), "strategy must not create a null map");
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

  @SuppressWarnings("unchecked")
  private static <T> LongObjectOpenAddressingStrategy<T> defaultStrategy() {
    return LongObjectOpenAddressingStrategy.LINEAR_PROBING;
  }
}

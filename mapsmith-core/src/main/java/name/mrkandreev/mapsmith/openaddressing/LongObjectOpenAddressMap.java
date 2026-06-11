package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;

public final class LongObjectOpenAddressMap<T> implements LongObjectMap<T> {
  public static final int DEFAULT_EXPECTED_SIZE = 16;

  private final LongObjectMap<T> delegate;

  public LongObjectOpenAddressMap() {
    this(defaultStrategy());
  }

  public LongObjectOpenAddressMap(int expectedSize) {
    this(defaultStrategy(), expectedSize);
  }

  public LongObjectOpenAddressMap(LongObjectOpenAddressingStrategy<T> strategy) {
    this(strategy, DEFAULT_EXPECTED_SIZE);
  }

  public LongObjectOpenAddressMap(LongObjectOpenAddressingStrategy<T> strategy, int expectedSize) {
    this(strategy, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public LongObjectOpenAddressMap(
      LongObjectOpenAddressingStrategy<T> strategy, LongHashing hashing) {
    this(strategy, DEFAULT_EXPECTED_SIZE, hashing);
  }

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

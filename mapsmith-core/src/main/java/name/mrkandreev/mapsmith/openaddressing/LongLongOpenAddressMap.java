package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongLongMap;

public final class LongLongOpenAddressMap implements LongLongMap {
  public static final int DEFAULT_EXPECTED_SIZE = 16;

  private final LongLongMap delegate;

  public LongLongOpenAddressMap() {
    this(LongLongOpenAddressingStrategy.LINEAR_PROBING);
  }

  public LongLongOpenAddressMap(int expectedSize) {
    this(LongLongOpenAddressingStrategy.LINEAR_PROBING, expectedSize);
  }

  public LongLongOpenAddressMap(LongLongOpenAddressingStrategy strategy) {
    this(strategy, DEFAULT_EXPECTED_SIZE);
  }

  public LongLongOpenAddressMap(LongLongOpenAddressingStrategy strategy, int expectedSize) {
    this(strategy, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public LongLongOpenAddressMap(LongLongOpenAddressingStrategy strategy, LongHashing hashing) {
    this(strategy, DEFAULT_EXPECTED_SIZE, hashing);
  }

  public LongLongOpenAddressMap(
      LongLongOpenAddressingStrategy strategy, int expectedSize, LongHashing hashing) {
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
  public long get(long key) {
    return delegate.get(key);
  }

  @Override
  public long getOrDefault(long key, long defaultValue) {
    return delegate.getOrDefault(key, defaultValue);
  }

  @Override
  public long put(long key, long value) {
    return delegate.put(key, value);
  }

  @Override
  public long remove(long key) {
    return delegate.remove(key);
  }

  @Override
  public void clear() {
    delegate.clear();
  }
}

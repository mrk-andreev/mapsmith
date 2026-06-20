package name.mrkandreev.mapsmith.openaddressing.strategies;

import java.util.Arrays;
import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;

public final class RobinHoodLongObjectMap<T> implements LongObjectMap<T> {
  private static final byte EMPTY = 0;
  private static final byte OCCUPIED = 1;

  private long[] keys;
  private Object[] values;
  private byte[] states;
  private int mask;
  private int resizeThreshold;
  private int entryCount;
  private final LongHashing hashing;

  public RobinHoodLongObjectMap() {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE);
  }

  public RobinHoodLongObjectMap(int expectedSize) {
    this(expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public RobinHoodLongObjectMap(LongHashing hashing) {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE, hashing);
  }

  public RobinHoodLongObjectMap(int expectedSize, LongHashing hashing) {
    this.hashing = Objects.requireNonNull(hashing, "hashing must not be null");
    allocate(LongLongHashSupport.capacityFor(expectedSize));
  }

  @Override
  public int size() {
    return entryCount;
  }

  @Override
  public boolean containsKey(long key) {
    return findIndex(key) >= 0;
  }

  @Override
  public T get(long key) {
    return getOrDefault(key, null);
  }

  @Override
  public T getOrDefault(long key, T defaultValue) {
    int index = findIndex(key);
    return index >= 0 ? valueAt(index) : defaultValue;
  }

  @Override
  public T put(long key, T value) {
    ensureInsertCapacity();
    return putWithoutResize(key, value);
  }

  @Override
  public T remove(long key) {
    int index = findIndex(key);
    if (index < 0) {
      return null;
    }

    T previousValue = valueAt(index);
    backwardShift(index);
    entryCount--;
    return previousValue;
  }

  @Override
  public void clear() {
    Arrays.fill(states, EMPTY);
    Arrays.fill(values, null);
    entryCount = 0;
  }

  private int findIndex(long key) {
    long hash = hashing.hash(key);
    int index = (int) hash & mask;
    int distance = 0;

    while (true) {
      if (states[index] == EMPTY) {
        return -1;
      }
      if (keys[index] == key) {
        return index;
      }
      if (probeDistance(index, keys[index]) < distance) {
        return -1;
      }

      index = (index + 1) & mask;
      distance++;
    }
  }

  private T putWithoutResize(long key, T value) {
    long currentKey = key;
    T currentValue = value;
    int index = (int) hashing.hash(currentKey) & mask;
    int distance = 0;

    while (true) {
      if (states[index] == EMPTY) {
        states[index] = OCCUPIED;
        keys[index] = currentKey;
        values[index] = currentValue;
        entryCount++;
        return null;
      }
      if (keys[index] == currentKey) {
        T previousValue = valueAt(index);
        values[index] = currentValue;
        return previousValue;
      }

      int existingDistance = probeDistance(index, keys[index]);
      if (existingDistance < distance) {
        long swappedKey = keys[index];
        T swappedValue = valueAt(index);
        keys[index] = currentKey;
        values[index] = currentValue;
        currentKey = swappedKey;
        currentValue = swappedValue;
        distance = existingDistance;
      }

      index = (index + 1) & mask;
      distance++;
    }
  }

  private void backwardShift(int deletedIndex) {
    int index = deletedIndex;
    int nextIndex = (index + 1) & mask;

    while (states[nextIndex] == OCCUPIED && probeDistance(nextIndex, keys[nextIndex]) > 0) {
      keys[index] = keys[nextIndex];
      values[index] = values[nextIndex];
      states[index] = OCCUPIED;
      index = nextIndex;
      nextIndex = (nextIndex + 1) & mask;
    }

    clearValue(index);
    states[index] = EMPTY;
  }

  private int probeDistance(int index, long key) {
    int idealIndex = (int) hashing.hash(key) & mask;
    return (index - idealIndex) & mask;
  }

  private void ensureInsertCapacity() {
    if (entryCount + 1 <= resizeThreshold) {
      return;
    }
    rehash(LongLongHashSupport.nextCapacity(keys.length));
  }

  private void rehash(int capacity) {
    long[] oldKeys = keys;
    Object[] oldValues = values;
    byte[] oldStates = states;

    allocate(capacity);

    for (int i = 0; i < oldStates.length; i++) {
      if (oldStates[i] == OCCUPIED) {
        putWithoutResize(oldKeys[i], valueFrom(oldValues[i]));
      }
    }
  }

  private void allocate(int capacity) {
    keys = new long[capacity];
    values = new Object[capacity];
    states = new byte[capacity];
    mask = capacity - 1;
    resizeThreshold = LongLongHashSupport.computeResizeThreshold(capacity);
    entryCount = 0;
  }

  @SuppressWarnings("unchecked")
  private T valueAt(int index) {
    return (T) values[index];
  }

  @SuppressWarnings("unchecked")
  private T valueFrom(Object value) {
    return (T) value;
  }

  private void clearValue(int index) {
    Arrays.fill(values, index, index + 1, null);
  }
}

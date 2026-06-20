package name.mrkandreev.mapsmith.openaddressing.strategies;

import java.util.Arrays;
import java.util.Objects;
import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;

public final class RobinHoodLongLongMap implements LongLongMap {
  private static final byte EMPTY = 0;
  private static final byte OCCUPIED = 1;

  private long[] keys;
  private long[] values;
  private byte[] states;
  private int mask;
  private int resizeThreshold;
  private int entryCount;
  private final LongHashing hashing;

  public RobinHoodLongLongMap() {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE);
  }

  public RobinHoodLongLongMap(int expectedSize) {
    this(expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public RobinHoodLongLongMap(LongHashing hashing) {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE, hashing);
  }

  public RobinHoodLongLongMap(int expectedSize, LongHashing hashing) {
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
  public long get(long key) {
    return getOrDefault(key, 0L);
  }

  @Override
  public long getOrDefault(long key, long defaultValue) {
    int index = findIndex(key);
    return index >= 0 ? values[index] : defaultValue;
  }

  @Override
  public long put(long key, long value) {
    ensureInsertCapacity();
    return putWithoutResize(key, value);
  }

  @Override
  public long remove(long key) {
    int index = findIndex(key);
    if (index < 0) {
      return 0L;
    }

    long previousValue = values[index];
    backwardShift(index);
    entryCount--;
    return previousValue;
  }

  @Override
  public void clear() {
    Arrays.fill(states, EMPTY);
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

  private long putWithoutResize(long key, long value) {
    long currentKey = key;
    long currentValue = value;
    int index = (int) hashing.hash(currentKey) & mask;
    int distance = 0;

    while (true) {
      if (states[index] == EMPTY) {
        states[index] = OCCUPIED;
        keys[index] = currentKey;
        values[index] = currentValue;
        entryCount++;
        return 0L;
      }
      if (keys[index] == currentKey) {
        long previousValue = values[index];
        values[index] = currentValue;
        return previousValue;
      }

      int existingDistance = probeDistance(index, keys[index]);
      if (existingDistance < distance) {
        long swappedKey = keys[index];
        long swappedValue = values[index];
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
    long[] oldValues = values;
    byte[] oldStates = states;

    allocate(capacity);

    for (int i = 0; i < oldStates.length; i++) {
      if (oldStates[i] == OCCUPIED) {
        putWithoutResize(oldKeys[i], oldValues[i]);
      }
    }
  }

  private void allocate(int capacity) {
    keys = new long[capacity];
    values = new long[capacity];
    states = new byte[capacity];
    mask = capacity - 1;
    resizeThreshold = LongLongHashSupport.computeResizeThreshold(capacity);
    entryCount = 0;
  }
}

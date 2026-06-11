package name.mrkandreev.mapsmith.openaddressing.strategies;

import java.util.Arrays;
import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;

public final class LinearProbingLongObjectMap<T> implements LongObjectMap<T> {
  private static final byte EMPTY = 0;
  private static final byte OCCUPIED = 1;
  private static final byte DELETED = 2;

  private long[] keys;
  private Object[] values;
  private byte[] states;
  private int mask;
  private int resizeThreshold;
  private int entryCount;
  private int used;
  private final LongHashing hashing;

  public LinearProbingLongObjectMap() {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE);
  }

  public LinearProbingLongObjectMap(int expectedSize) {
    this(expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public LinearProbingLongObjectMap(LongHashing hashing) {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE, hashing);
  }

  public LinearProbingLongObjectMap(int expectedSize, LongHashing hashing) {
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

    int index = insertionIndex(key);
    if (index >= 0) {
      T previousValue = valueAt(index);
      values[index] = value;
      return previousValue;
    }

    int insertionIndex = -index - 1;
    if (states[insertionIndex] == EMPTY) {
      used++;
    }
    states[insertionIndex] = OCCUPIED;
    keys[insertionIndex] = key;
    values[insertionIndex] = value;
    entryCount++;
    return null;
  }

  @Override
  public T remove(long key) {
    int index = findIndex(key);
    if (index < 0) {
      return null;
    }

    T previousValue = valueAt(index);
    clearValue(index);
    states[index] = DELETED;
    entryCount--;
    return previousValue;
  }

  @Override
  public void clear() {
    Arrays.fill(states, EMPTY);
    Arrays.fill(values, null);
    entryCount = 0;
    used = 0;
  }

  private int findIndex(long key) {
    int index = (int) hashing.hash(key) & mask;
    while (true) {
      byte state = states[index];
      if (state == EMPTY) {
        return -1;
      }
      if (state == OCCUPIED && keys[index] == key) {
        return index;
      }
      index = (index + 1) & mask;
    }
  }

  private int insertionIndex(long key) {
    int index = (int) hashing.hash(key) & mask;
    int firstDeletedIndex = -1;

    while (true) {
      byte state = states[index];
      if (state == EMPTY) {
        return firstDeletedIndex >= 0 ? -firstDeletedIndex - 1 : -index - 1;
      }
      if (state == OCCUPIED && keys[index] == key) {
        return index;
      }
      if (state == DELETED && firstDeletedIndex < 0) {
        firstDeletedIndex = index;
      }
      index = (index + 1) & mask;
    }
  }

  private void ensureInsertCapacity() {
    if (used + 1 <= resizeThreshold) {
      return;
    }

    int nextCapacity =
        entryCount + 1 <= resizeThreshold
            ? keys.length
            : LongLongHashSupport.nextCapacity(keys.length);
    rehash(nextCapacity);
  }

  private void rehash(int capacity) {
    long[] oldKeys = keys;
    Object[] oldValues = values;
    byte[] oldStates = states;

    allocate(capacity);

    for (int i = 0; i < oldStates.length; i++) {
      if (oldStates[i] == OCCUPIED) {
        insertRehashed(oldKeys[i], valueFrom(oldValues[i]));
      }
    }
  }

  private void insertRehashed(long key, T value) {
    int index = (int) hashing.hash(key) & mask;
    while (states[index] == OCCUPIED) {
      index = (index + 1) & mask;
    }

    states[index] = OCCUPIED;
    keys[index] = key;
    values[index] = value;
    entryCount++;
    used++;
  }

  private void allocate(int capacity) {
    keys = new long[capacity];
    values = new Object[capacity];
    states = new byte[capacity];
    mask = capacity - 1;
    resizeThreshold = LongLongHashSupport.computeResizeThreshold(capacity);
    entryCount = 0;
    used = 0;
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

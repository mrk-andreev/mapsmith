package name.mrkandreev.mapsmith.openaddressing.strategies;

import java.util.Arrays;
import java.util.Objects;
import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;

/** A primitive long-to-long map using linear probing. */
public final class LinearProbingLongLongMap implements LongLongMap {
  private static final byte EMPTY = 0;
  private static final byte OCCUPIED = 1;
  private static final byte DELETED = 2;

  private long[] keys;
  private long[] values;
  private byte[] states;
  private int mask;
  private int resizeThreshold;
  private int entryCount;
  private int used;
  private final LongHashing hashing;

  /** Creates a map with default capacity and hashing. */
  public LinearProbingLongLongMap() {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE);
  }

  /**
   * Creates a map.
   *
   * @param expectedSize expected entry count
   */
  public LinearProbingLongLongMap(int expectedSize) {
    this(expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  /**
   * Creates a map.
   *
   * @param hashing hash function
   */
  public LinearProbingLongLongMap(LongHashing hashing) {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE, hashing);
  }

  /**
   * Creates a map.
   *
   * @param expectedSize expected entry count
   * @param hashing hash function
   */
  public LinearProbingLongLongMap(int expectedSize, LongHashing hashing) {
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

    int index = insertionIndex(key);
    if (index >= 0) {
      long previousValue = values[index];
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
    return 0L;
  }

  @Override
  public long remove(long key) {
    int index = findIndex(key);
    if (index < 0) {
      return 0L;
    }

    long previousValue = values[index];
    states[index] = DELETED;
    entryCount--;
    return previousValue;
  }

  @Override
  public void clear() {
    Arrays.fill(states, EMPTY);
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
    long[] oldValues = values;
    byte[] oldStates = states;

    allocate(capacity);

    for (int i = 0; i < oldStates.length; i++) {
      if (oldStates[i] == OCCUPIED) {
        insertRehashed(oldKeys[i], oldValues[i]);
      }
    }
  }

  private void insertRehashed(long key, long value) {
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
    values = new long[capacity];
    states = new byte[capacity];
    mask = capacity - 1;
    resizeThreshold = LongLongHashSupport.computeResizeThreshold(capacity);
    entryCount = 0;
    used = 0;
  }
}

package name.mrkandreev.mapsmith.openaddressing.strategies;

import java.util.Arrays;
import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;

public final class SwissTableLongObjectMap<T> implements LongObjectMap<T> {
  private static final byte EMPTY = 0;
  private static final byte DELETED = 1;

  private long[] keys;
  private Object[] values;
  private byte[] controls;
  private int mask;
  private int resizeThreshold;
  private int entryCount;
  private int used;
  private final LongHashing hashing;

  public SwissTableLongObjectMap() {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE);
  }

  public SwissTableLongObjectMap(int expectedSize) {
    this(expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public SwissTableLongObjectMap(LongHashing hashing) {
    this(LongLongHashSupport.DEFAULT_EXPECTED_SIZE, hashing);
  }

  public SwissTableLongObjectMap(int expectedSize, LongHashing hashing) {
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

    long hash = hashing.hash(key);
    byte fingerprint = fingerprint(hash);
    int index = insertionIndex(key, hash, fingerprint);
    if (index >= 0) {
      T previousValue = valueAt(index);
      values[index] = value;
      return previousValue;
    }

    int insertionIndex = -index - 1;
    if (controls[insertionIndex] == EMPTY) {
      used++;
    }
    controls[insertionIndex] = fingerprint;
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
    controls[index] = DELETED;
    entryCount--;
    return previousValue;
  }

  @Override
  public void clear() {
    Arrays.fill(controls, EMPTY);
    Arrays.fill(values, null);
    entryCount = 0;
    used = 0;
  }

  private int findIndex(long key) {
    long hash = hashing.hash(key);
    byte fingerprint = fingerprint(hash);
    int index = (int) hash & mask;

    while (true) {
      byte control = controls[index];
      if (control == EMPTY) {
        return -1;
      }
      if (control == fingerprint && keys[index] == key) {
        return index;
      }
      index = (index + 1) & mask;
    }
  }

  private int insertionIndex(long key, long hash, byte fingerprint) {
    int index = (int) hash & mask;
    int firstDeletedIndex = -1;

    while (true) {
      byte control = controls[index];
      if (control == EMPTY) {
        return firstDeletedIndex >= 0 ? -firstDeletedIndex - 1 : -index - 1;
      }
      if (control == fingerprint && keys[index] == key) {
        return index;
      }
      if (control == DELETED && firstDeletedIndex < 0) {
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
    byte[] oldControls = controls;

    allocate(capacity);

    for (int i = 0; i < oldControls.length; i++) {
      if (isFull(oldControls[i])) {
        insertRehashed(oldKeys[i], valueFrom(oldValues[i]));
      }
    }
  }

  private void insertRehashed(long key, T value) {
    long hash = hashing.hash(key);
    byte fingerprint = fingerprint(hash);
    int index = (int) hash & mask;

    while (isFull(controls[index])) {
      index = (index + 1) & mask;
    }

    controls[index] = fingerprint;
    keys[index] = key;
    values[index] = value;
    entryCount++;
    used++;
  }

  private void allocate(int capacity) {
    keys = new long[capacity];
    values = new Object[capacity];
    controls = new byte[capacity];
    mask = capacity - 1;
    resizeThreshold = LongLongHashSupport.computeResizeThreshold(capacity);
    entryCount = 0;
    used = 0;
  }

  private static boolean isFull(byte control) {
    return control != EMPTY && control != DELETED;
  }

  private static byte fingerprint(long hash) {
    int result = (int) (hash >>> 57) + 2;
    return (byte) result;
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

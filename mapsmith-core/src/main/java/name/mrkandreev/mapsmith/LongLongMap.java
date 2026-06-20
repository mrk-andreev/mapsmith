package name.mrkandreev.mapsmith;

public interface LongLongMap {
  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  boolean containsKey(long key);

  long get(long key);

  long getOrDefault(long key, long defaultValue);

  long put(long key, long value);

  long remove(long key);

  void clear();
}

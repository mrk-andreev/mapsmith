package name.mrkandreev.mapsmith;

public interface LongObjectMap<T> {
  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  boolean containsKey(long key);

  T get(long key);

  T getOrDefault(long key, T defaultValue);

  T put(long key, T value);

  T remove(long key);

  void clear();
}

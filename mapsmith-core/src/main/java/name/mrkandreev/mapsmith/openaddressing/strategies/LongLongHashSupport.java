package name.mrkandreev.mapsmith.openaddressing.strategies;

final class LongLongHashSupport {
  static final int DEFAULT_EXPECTED_SIZE = 16;
  static final int MIN_CAPACITY = 2;
  static final int LOAD_FACTOR_NUMERATOR = 2;
  static final int LOAD_FACTOR_DENOMINATOR = 3;
  static final int MAX_CAPACITY = 1 << 30;

  private LongLongHashSupport() {}

  static int capacityFor(int expectedSize) {
    if (expectedSize < 0) {
      throw new IllegalArgumentException("expectedSize must be non-negative");
    }
    if (expectedSize == 0) {
      return MIN_CAPACITY;
    }

    long capacity = ((long) expectedSize * LOAD_FACTOR_DENOMINATOR / LOAD_FACTOR_NUMERATOR) + 1L;
    if (capacity > MAX_CAPACITY) {
      throw new IllegalArgumentException("expectedSize is too large");
    }

    return tableSizeFor((int) capacity);
  }

  static int nextCapacity(int capacity) {
    if (capacity >= MAX_CAPACITY) {
      throw new IllegalStateException("maximum capacity reached");
    }
    return capacity << 1;
  }

  static int computeResizeThreshold(int capacity) {
    return Math.max(1, capacity * LOAD_FACTOR_NUMERATOR / LOAD_FACTOR_DENOMINATOR);
  }

  private static int tableSizeFor(int capacity) {
    int result = MIN_CAPACITY;
    while (result < capacity) {
      result <<= 1;
    }
    return result;
  }
}

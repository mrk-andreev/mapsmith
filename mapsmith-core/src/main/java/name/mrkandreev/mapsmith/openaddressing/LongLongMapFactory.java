package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongLongMap;

/** Factory for primitive long-to-long maps. */
public final class LongLongMapFactory {
  private LongLongMapFactory() {}

  /**
   * Creates a map using the default hash function.
   *
   * @param specialization implementation to use
   * @return new map
   */
  public static LongLongMap create(MapSpecialization specialization) {
    return create(specialization, LongHashing.MURMUR3_FINALIZER);
  }

  /**
   * Creates a map using the default hash function.
   *
   * @param specialization implementation to use
   * @param expectedSize expected entry count
   * @return new map
   */
  public static LongLongMap create(MapSpecialization specialization, int expectedSize) {
    return create(specialization, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  /**
   * Creates a map.
   *
   * @param specialization implementation to use
   * @param hashing hash function
   * @return new map
   */
  public static LongLongMap create(MapSpecialization specialization, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongLongOpenAddressMap(specialization.strategy(), hashing);
  }

  /**
   * Creates a map.
   *
   * @param specialization implementation to use
   * @param expectedSize expected entry count
   * @param hashing hash function
   * @return new map
   */
  public static LongLongMap create(
      MapSpecialization specialization, int expectedSize, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongLongOpenAddressMap(specialization.strategy(), expectedSize, hashing);
  }
}

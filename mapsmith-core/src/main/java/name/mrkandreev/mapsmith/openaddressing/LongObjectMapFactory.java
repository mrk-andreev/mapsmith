package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongObjectMap;

public final class LongObjectMapFactory {
  private LongObjectMapFactory() {}

  public static <T> LongObjectMap<T> create(MapSpecialization specialization) {
    return create(specialization, LongHashing.MURMUR3_FINALIZER);
  }

  public static <T> LongObjectMap<T> create(MapSpecialization specialization, int expectedSize) {
    return create(specialization, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public static <T> LongObjectMap<T> create(MapSpecialization specialization, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongObjectOpenAddressMap<>(specialization.objectStrategy(), hashing);
  }

  public static <T> LongObjectMap<T> create(
      MapSpecialization specialization, int expectedSize, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongObjectOpenAddressMap<>(specialization.objectStrategy(), expectedSize, hashing);
  }
}

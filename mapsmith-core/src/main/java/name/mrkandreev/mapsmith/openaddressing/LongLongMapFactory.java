package name.mrkandreev.mapsmith.openaddressing;

import java.util.Objects;
import name.mrkandreev.mapsmith.LongLongMap;

public final class LongLongMapFactory {
  private LongLongMapFactory() {}

  public static LongLongMap create(MapSpecialization specialization) {
    return create(specialization, LongHashing.MURMUR3_FINALIZER);
  }

  public static LongLongMap create(MapSpecialization specialization, int expectedSize) {
    return create(specialization, expectedSize, LongHashing.MURMUR3_FINALIZER);
  }

  public static LongLongMap create(MapSpecialization specialization, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongLongOpenAddressMap(specialization.strategy(), hashing);
  }

  public static LongLongMap create(
      MapSpecialization specialization, int expectedSize, LongHashing hashing) {
    Objects.requireNonNull(specialization, "specialization must not be null");
    Objects.requireNonNull(hashing, "hashing must not be null");

    return new LongLongOpenAddressMap(specialization.strategy(), expectedSize, hashing);
  }
}

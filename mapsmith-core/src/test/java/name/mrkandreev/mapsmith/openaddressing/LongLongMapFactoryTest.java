package name.mrkandreev.mapsmith.openaddressing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongLongMap;
import org.junit.jupiter.api.Test;

class LongLongMapFactoryTest {
  @Test
  void createsLinearProbingMaps() {
    assertThat(LongLongMapFactory.create(MapSpecialization.LINEAR_PROBING))
        .isInstanceOf(LongLongOpenAddressMap.class);
    assertThat(LongLongMapFactory.create(MapSpecialization.LINEAR_PROBING, 32))
        .isInstanceOf(LongLongOpenAddressMap.class);
  }

  @Test
  void createsRobinHoodMaps() {
    assertThat(LongLongMapFactory.create(MapSpecialization.ROBIN_HOOD))
        .isInstanceOf(LongLongOpenAddressMap.class);
    assertThat(LongLongMapFactory.create(MapSpecialization.ROBIN_HOOD, 32))
        .isInstanceOf(LongLongOpenAddressMap.class);
  }

  @Test
  void createsSwissTableMaps() {
    assertThat(LongLongMapFactory.create(MapSpecialization.SWISS_TABLE))
        .isInstanceOf(LongLongOpenAddressMap.class);
    assertThat(LongLongMapFactory.create(MapSpecialization.SWISS_TABLE, 32))
        .isInstanceOf(LongLongOpenAddressMap.class);
  }

  @Test
  void createdMapsCanStoreValues() {
    LongLongMap map =
        LongLongMapFactory.create(MapSpecialization.SWISS_TABLE, 32, LongHashing.FIBONACCI);

    assertThat(map.put(1L, 10L)).isZero();
    assertThat(map.get(1L)).isEqualTo(10L);
  }

  @Test
  void passesExpectedSizeToSpecialization() {
    assertThatThrownBy(() -> LongLongMapFactory.create(MapSpecialization.LINEAR_PROBING, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize must be non-negative");
  }

  @Test
  void openAddressMapAcceptsCustomStrategies() {
    LongLongOpenAddressingStrategy strategy =
        (expectedSize, hashing) -> {
          assertThat(expectedSize).isEqualTo(32);
          assertThat(hashing).isSameAs(LongHashing.IDENTITY);
          return new LinearProbingLongLongMap(expectedSize, hashing);
        };

    LongLongMap map = new LongLongOpenAddressMap(strategy, 32, LongHashing.IDENTITY);

    assertThat(map.put(7L, 70L)).isZero();
    assertThat(map.get(7L)).isEqualTo(70L);
  }

  @Test
  void openAddressMapRejectsNullStrategy() {
    assertThatThrownBy(() -> new LongLongOpenAddressMap(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("strategy must not be null");
  }

  @Test
  void openAddressMapRejectsNullStrategyResult() {
    LongLongOpenAddressingStrategy strategy = (expectedSize, hashing) -> null;

    assertThatThrownBy(() -> new LongLongOpenAddressMap(strategy))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("strategy must not create a null map");
  }

  @Test
  void rejectsNullSpecialization() {
    assertThatThrownBy(() -> LongLongMapFactory.create((MapSpecialization) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("specialization must not be null");
  }

  @Test
  void rejectsNullHashing() {
    assertThatThrownBy(() -> LongLongMapFactory.create(MapSpecialization.LINEAR_PROBING, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("hashing must not be null");
  }
}

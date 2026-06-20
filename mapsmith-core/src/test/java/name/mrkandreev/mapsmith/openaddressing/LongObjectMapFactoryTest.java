package name.mrkandreev.mapsmith.openaddressing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongObjectMap;
import org.junit.jupiter.api.Test;

class LongObjectMapFactoryTest {
  @Test
  void createsMaps() {
    assertThat(LongObjectMapFactory.create(MapSpecialization.LINEAR_PROBING))
        .isInstanceOf(LongObjectOpenAddressMap.class);
    assertThat(LongObjectMapFactory.create(MapSpecialization.ROBIN_HOOD, 32))
        .isInstanceOf(LongObjectOpenAddressMap.class);
    assertThat(LongObjectMapFactory.create(MapSpecialization.SWISS_TABLE, LongHashing.FIBONACCI))
        .isInstanceOf(LongObjectOpenAddressMap.class);
  }

  @Test
  void createdMapsCanStoreValues() {
    LongObjectMap<String> map =
        LongObjectMapFactory.create(MapSpecialization.SWISS_TABLE, 32, LongHashing.FIBONACCI);

    assertThat(map.put(1L, "one")).isNull();
    assertThat(map.get(1L)).isEqualTo("one");
  }

  @Test
  void passesExpectedSizeToSpecialization() {
    assertThatThrownBy(() -> LongObjectMapFactory.create(MapSpecialization.LINEAR_PROBING, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize must be non-negative");
  }

  @Test
  void openAddressMapAcceptsCustomStrategies() {
    LongObjectOpenAddressingStrategy<String> strategy =
        (expectedSize, hashing) -> {
          assertThat(expectedSize).isEqualTo(32);
          assertThat(hashing).isSameAs(LongHashing.IDENTITY);
          return new LinearProbingLongObjectMap<>(expectedSize, hashing);
        };

    LongObjectMap<String> map = new LongObjectOpenAddressMap<>(strategy, 32, LongHashing.IDENTITY);

    assertThat(map.put(7L, "seven")).isNull();
    assertThat(map.get(7L)).isEqualTo("seven");
  }

  @Test
  void openAddressMapRejectsNullStrategy() {
    assertThatThrownBy(() -> new LongObjectOpenAddressMap<String>(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("strategy must not be null");
  }

  @Test
  void openAddressMapRejectsNullStrategyResult() {
    LongObjectOpenAddressingStrategy<String> strategy = (expectedSize, hashing) -> null;

    assertThatThrownBy(() -> new LongObjectOpenAddressMap<String>(strategy))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("strategy must not create a null map");
  }

  @Test
  void rejectsNullSpecialization() {
    assertThatThrownBy(() -> LongObjectMapFactory.create((MapSpecialization) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("specialization must not be null");
  }

  @Test
  void rejectsNullHashing() {
    assertThatThrownBy(() -> LongObjectMapFactory.create(MapSpecialization.LINEAR_PROBING, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("hashing must not be null");
  }
}

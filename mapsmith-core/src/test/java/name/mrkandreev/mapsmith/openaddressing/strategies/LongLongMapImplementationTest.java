package name.mrkandreev.mapsmith.openaddressing.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressMap;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressingStrategy;
import name.mrkandreev.mapsmith.ranking.OrderStatisticLongLongMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongLongMapImplementationTest {
  private static final String MAP_IMPLEMENTATIONS = "mapImplementations";
  private static final String HASHING_METHOD_SOURCE = "hashings";

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void startsEmpty(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);

    assertThat(map.size()).as(name).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(10L)).isZero();
    assertThat(map.getOrDefault(10L, 42L)).isEqualTo(42L);
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void rejectsNegativeExpectedSize(String name, IntFunction<LongLongMap> maps) {
    assertThatThrownBy(() -> maps.apply(-1))
        .as(name)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize must be non-negative");
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void putsAndGetsValues(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);

    assertThat(map.put(10L, 100L)).as(name).isZero();
    assertThat(map.put(-1L, -100L)).isZero();

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.isEmpty()).isFalse();
    assertThat(map.containsKey(10L)).isTrue();
    assertThat(map.containsKey(-1L)).isTrue();
    assertThat(map.get(10L)).isEqualTo(100L);
    assertThat(map.get(-1L)).isEqualTo(-100L);
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void returnsPreviousValueWhenUpdating(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);

    assertThat(map.put(7L, 70L)).as(name).isZero();
    assertThat(map.put(7L, 700L)).isEqualTo(70L);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(7L)).isEqualTo(700L);
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void supportsZeroAsKeyAndValue(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);

    assertThat(map.put(0L, 0L)).as(name).isZero();

    assertThat(map.containsKey(0L)).isTrue();
    assertThat(map.get(0L)).isZero();
    assertThat(map.getOrDefault(0L, 99L)).isZero();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void removesValues(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);
    map.put(1L, 10L);
    map.put(2L, 20L);

    assertThat(map.remove(1L)).as(name).isEqualTo(10L);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.containsKey(1L)).isFalse();
    assertThat(map.containsKey(2L)).isTrue();
    assertThat(map.remove(1L)).isZero();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void keepsProbeChainSearchableAfterRemove(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(1);

    for (long key = 0; key < 100; key++) {
      map.put(key, key * 10L);
    }
    for (long key = 0; key < 100; key += 2) {
      assertThat(map.remove(key)).as(name).isEqualTo(key * 10L);
    }

    for (long key = 1; key < 100; key += 2) {
      assertThat(map.containsKey(key)).isTrue();
      assertThat(map.get(key)).isEqualTo(key * 10L);
    }
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void reusesDeletedOrShiftedSlots(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(1);

    for (long key = 0; key < 50; key++) {
      map.put(key, key);
    }
    for (long key = 0; key < 50; key++) {
      assertThat(map.remove(key)).as(name).isEqualTo(key);
    }
    for (long key = 50; key < 100; key++) {
      map.put(key, key * 2L);
    }

    assertThat(map.size()).isEqualTo(50);
    for (long key = 50; key < 100; key++) {
      assertThat(map.get(key)).isEqualTo(key * 2L);
    }
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void growsWhenLoadThresholdIsReached(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(1);

    for (long key = 0; key < 10_000; key++) {
      assertThat(map.put(key, key + 1L)).as(name).isZero();
    }

    assertThat(map.size()).isEqualTo(10_000);
    for (long key = 0; key < 10_000; key++) {
      assertThat(map.get(key)).isEqualTo(key + 1L);
    }
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void clearsValues(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(16);
    map.put(1L, 10L);
    map.put(2L, 20L);

    map.clear();

    assertThat(map.size()).as(name).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(1L)).isFalse();
    assertThat(map.containsKey(2L)).isFalse();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void supportsZeroExpectedSize(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(0);

    assertThat(map.put(1L, 1L)).as(name).isZero();
  }

  @Nested
  class LinearProbingConstructors {
    @Test
    void createsMapWithDefaultExpectedSize() {
      assertThat(new LinearProbingLongLongMap()).isNotNull();
    }

    @Test
    void createsMapWithSpecifiedExpectedSize() {
      assertThat(new LinearProbingLongLongMap(1)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource(
        "name.mrkandreev.mapsmith.openaddressing.strategies.LongLongMapImplementationTest#hashings")
    void createsMapWithSpecifiedHashing(LongHashing hashing) {
      assertThat(new LinearProbingLongLongMap(hashing)).isNotNull();
    }
  }

  @Nested
  class RobinHoodConstructors {
    @Test
    void createsMapWithDefaultExpectedSize() {
      assertThat(new RobinHoodLongLongMap()).isNotNull();
    }

    @Test
    void createsMapWithSpecifiedExpectedSize() {
      assertThat(new RobinHoodLongLongMap(1)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource(
        "name.mrkandreev.mapsmith.openaddressing.strategies.LongLongMapImplementationTest#hashings")
    void createsMapWithSpecifiedHashing(LongHashing hashing) {
      assertThat(new RobinHoodLongLongMap(hashing)).isNotNull();
    }
  }

  @ParameterizedTest
  @MethodSource(HASHING_METHOD_SOURCE)
  void supportsSwissTableConstructors(LongHashing hashing) {
    assertThat(new SwissTableLongLongMap()).isNotNull();
    assertThat(new SwissTableLongLongMap(1)).isNotNull();
    assertThat(new SwissTableLongLongMap(hashing)).isNotNull();
  }

  @ParameterizedTest
  @MethodSource(HASHING_METHOD_SOURCE)
  void supportsOpenAddressMapConstructors(LongHashing hashing) {
    assertThat(new LongLongOpenAddressMap()).isNotNull();
    assertThat(new LongLongOpenAddressMap(1)).isNotNull();
    assertThat(new LongLongOpenAddressMap(LongLongOpenAddressingStrategy.SWISS_TABLE)).isNotNull();
    assertThat(new LongLongOpenAddressMap(LongLongOpenAddressingStrategy.SWISS_TABLE, 1))
        .isNotNull();
    assertThat(new LongLongOpenAddressMap(LongLongOpenAddressingStrategy.SWISS_TABLE, hashing))
        .isNotNull();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void matchesHashMapForRandomOperations(String name, IntFunction<LongLongMap> maps) {
    LongLongMap map = maps.apply(1);
    Map<Long, Long> expected = new HashMap<>();
    SplittableRandom random = new SplittableRandom(0x6d6170736d697468L);

    for (int i = 0; i < 20_000; i++) {
      long key = random.nextLong(2_000L);
      long value = random.nextLong();
      int operation = random.nextInt(3);

      switch (operation) {
        case 0 -> {
          Long previousValue = expected.put(key, value);
          assertThat(map.put(key, value))
              .as(name)
              .isEqualTo(previousValue == null ? 0L : previousValue);
        }
        case 1 -> {
          Long previousValue = expected.remove(key);
          assertThat(map.remove(key))
              .as(name)
              .isEqualTo(previousValue == null ? 0L : previousValue);
        }
        default -> {
          assertThat(map.containsKey(key)).as(name).isEqualTo(expected.containsKey(key));
          assertThat(map.getOrDefault(key, Long.MIN_VALUE))
              .isEqualTo(expected.getOrDefault(key, Long.MIN_VALUE));
        }
      }

      assertThat(map.size()).as(name).isEqualTo(expected.size());
    }
  }

  private static Stream<Arguments> mapImplementations() {
    return Arrays.stream(LongHashing.values())
        .flatMap(
            hashing ->
                Stream.of(
                    Arguments.of(
                        "linear probing " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize -> new LinearProbingLongLongMap(expectedSize, hashing)),
                    Arguments.of(
                        "robin hood " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize -> new RobinHoodLongLongMap(expectedSize, hashing)),
                    Arguments.of(
                        "swiss table " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize -> new SwissTableLongLongMap(expectedSize, hashing)),
                    Arguments.of(
                        "open address linear probing " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize ->
                                new LongLongOpenAddressMap(
                                    LinearProbingLongLongMap::new, expectedSize, hashing)),
                    Arguments.of(
                        "open address robin hood " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize ->
                                new LongLongOpenAddressMap(
                                    RobinHoodLongLongMap::new, expectedSize, hashing)),
                    Arguments.of(
                        "open address swiss table " + hashing,
                        (IntFunction<LongLongMap>)
                            expectedSize ->
                                new LongLongOpenAddressMap(
                                    SwissTableLongLongMap::new, expectedSize, hashing)),
                    Arguments.of(
                        "order statistic " + hashing,
                        (IntFunction<LongLongMap>) OrderStatisticLongLongMap::new)));
  }

  private static Stream<LongHashing> hashings() {
    return Arrays.stream(LongHashing.values());
  }
}

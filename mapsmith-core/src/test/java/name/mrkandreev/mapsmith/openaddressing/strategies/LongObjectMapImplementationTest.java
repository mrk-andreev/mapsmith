package name.mrkandreev.mapsmith.openaddressing.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongObjectOpenAddressMap;
import name.mrkandreev.mapsmith.openaddressing.LongObjectOpenAddressingStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongObjectMapImplementationTest {
  private static final String MAP_IMPLEMENTATIONS = "mapImplementations";
  private static final String HASHING_METHOD_SOURCE = "hashings";
  private static final String VALUE_PREFIX = "value-";

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void startsEmpty(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);

    assertThat(map.size()).as(name).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(10L)).isNull();
    assertThat(map.getOrDefault(10L, "fallback")).isEqualTo("fallback");
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void rejectsNegativeExpectedSize(String name, IntFunction<LongObjectMap<String>> maps) {
    assertThatThrownBy(() -> maps.apply(-1))
        .as(name)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize must be non-negative");
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void putsAndGetsValues(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);

    assertThat(map.put(10L, "ten")).as(name).isNull();
    assertThat(map.put(-1L, "minus-one")).isNull();

    assertThat(map.size()).isEqualTo(2);
    assertThat(map.isEmpty()).isFalse();
    assertThat(map.containsKey(10L)).isTrue();
    assertThat(map.containsKey(-1L)).isTrue();
    assertThat(map.get(10L)).isEqualTo("ten");
    assertThat(map.get(-1L)).isEqualTo("minus-one");
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void returnsPreviousValueWhenUpdating(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);

    assertThat(map.put(7L, "seven")).as(name).isNull();
    assertThat(map.put(7L, "updated")).isEqualTo("seven");

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(7L)).isEqualTo("updated");
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void supportsNullValues(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);

    assertThat(map.put(0L, null)).as(name).isNull();

    assertThat(map.containsKey(0L)).isTrue();
    assertThat(map.get(0L)).isNull();
    assertThat(map.getOrDefault(0L, "fallback")).isNull();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void removesValues(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);
    map.put(1L, "one");
    map.put(2L, "two");

    assertThat(map.remove(1L)).as(name).isEqualTo("one");

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.containsKey(1L)).isFalse();
    assertThat(map.containsKey(2L)).isTrue();
    assertThat(map.remove(1L)).isNull();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void keepsProbeChainSearchableAfterRemove(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(1);

    for (long key = 0; key < 100; key++) {
      map.put(key, VALUE_PREFIX + key);
    }
    for (long key = 0; key < 100; key += 2) {
      assertThat(map.remove(key)).as(name).isEqualTo(VALUE_PREFIX + key);
    }

    for (long key = 1; key < 100; key += 2) {
      assertThat(map.containsKey(key)).isTrue();
      assertThat(map.get(key)).isEqualTo(VALUE_PREFIX + key);
    }
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void growsWhenLoadThresholdIsReached(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(1);

    for (long key = 0; key < 10_000; key++) {
      assertThat(map.put(key, VALUE_PREFIX + key)).as(name).isNull();
    }

    assertThat(map.size()).isEqualTo(10_000);
    for (long key = 0; key < 10_000; key++) {
      assertThat(map.get(key)).isEqualTo(VALUE_PREFIX + key);
    }
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void clearsValues(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(16);
    map.put(1L, "one");
    map.put(2L, "two");

    map.clear();

    assertThat(map.size()).as(name).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(1L)).isFalse();
    assertThat(map.containsKey(2L)).isFalse();
  }

  @Nested
  class LinearProbingConstructors {
    @Test
    void createsMapWithDefaultExpectedSize() {
      assertThat(new LinearProbingLongObjectMap<String>()).isNotNull();
    }

    @Test
    void createsMapWithSpecifiedExpectedSize() {
      assertThat(new LinearProbingLongObjectMap<String>(1)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource(
        "name.mrkandreev.mapsmith.openaddressing.strategies.LongObjectMapImplementationTest#hashings")
    void createsMapWithSpecifiedHashing(LongHashing hashing) {
      assertThat(new LinearProbingLongObjectMap<String>(hashing)).isNotNull();
    }
  }

  @Nested
  class RobinHoodConstructors {
    @Test
    void createsMapWithDefaultExpectedSize() {
      assertThat(new RobinHoodLongObjectMap<String>()).isNotNull();
    }

    @Test
    void createsMapWithSpecifiedExpectedSize() {
      assertThat(new RobinHoodLongObjectMap<String>(1)).isNotNull();
    }

    @ParameterizedTest
    @MethodSource(
        "name.mrkandreev.mapsmith.openaddressing.strategies.LongObjectMapImplementationTest#hashings")
    void createsMapWithSpecifiedHashing(LongHashing hashing) {
      assertThat(new RobinHoodLongObjectMap<String>(hashing)).isNotNull();
    }
  }

  @ParameterizedTest
  @MethodSource(HASHING_METHOD_SOURCE)
  void supportsSwissTableConstructors(LongHashing hashing) {
    assertThat(new SwissTableLongObjectMap<String>()).isNotNull();
    assertThat(new SwissTableLongObjectMap<String>(1)).isNotNull();
    assertThat(new SwissTableLongObjectMap<String>(hashing)).isNotNull();
  }

  @ParameterizedTest
  @MethodSource(HASHING_METHOD_SOURCE)
  void supportsOpenAddressMapConstructors(LongHashing hashing) {
    @SuppressWarnings(
        "unchecked") // Strategy constants are necessarily raw generic factory instances.
    LongObjectOpenAddressingStrategy<String> swissTable =
        LongObjectOpenAddressingStrategy.SWISS_TABLE;

    assertThat(new LongObjectOpenAddressMap<String>()).isNotNull();
    assertThat(new LongObjectOpenAddressMap<String>(1)).isNotNull();
    assertThat(new LongObjectOpenAddressMap<String>(swissTable)).isNotNull();
    assertThat(new LongObjectOpenAddressMap<String>(swissTable, 1)).isNotNull();
    assertThat(new LongObjectOpenAddressMap<String>(swissTable, hashing)).isNotNull();
  }

  @ParameterizedTest
  @MethodSource(MAP_IMPLEMENTATIONS)
  void matchesHashMapForRandomOperations(String name, IntFunction<LongObjectMap<String>> maps) {
    LongObjectMap<String> map = maps.apply(1);
    Map<Long, String> expected = new HashMap<>();
    SplittableRandom random = new SplittableRandom(0x6d6170736d697468L);

    for (int i = 0; i < 20_000; i++) {
      long key = random.nextLong(2_000L);
      String value = VALUE_PREFIX + random.nextLong();
      int operation = random.nextInt(3);

      switch (operation) {
        case 0 -> assertThat(map.put(key, value)).as(name).isEqualTo(expected.put(key, value));
        case 1 -> assertThat(map.remove(key)).as(name).isEqualTo(expected.remove(key));
        default -> {
          assertThat(map.containsKey(key)).as(name).isEqualTo(expected.containsKey(key));
          assertThat(map.getOrDefault(key, "missing"))
              .isEqualTo(expected.getOrDefault(key, "missing"));
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
                    mapImplementation(
                        "linear probing " + hashing,
                        expectedSize -> new LinearProbingLongObjectMap<>(expectedSize, hashing)),
                    mapImplementation(
                        "robin hood " + hashing,
                        expectedSize -> new RobinHoodLongObjectMap<>(expectedSize, hashing)),
                    mapImplementation(
                        "swiss table " + hashing,
                        expectedSize -> new SwissTableLongObjectMap<>(expectedSize, hashing)),
                    mapImplementation(
                        "open address linear probing " + hashing,
                        expectedSize ->
                            new LongObjectOpenAddressMap<>(
                                LinearProbingLongObjectMap::new, expectedSize, hashing)),
                    mapImplementation(
                        "open address robin hood " + hashing,
                        expectedSize ->
                            new LongObjectOpenAddressMap<>(
                                RobinHoodLongObjectMap::new, expectedSize, hashing)),
                    mapImplementation(
                        "open address swiss table " + hashing,
                        expectedSize ->
                            new LongObjectOpenAddressMap<>(
                                SwissTableLongObjectMap::new, expectedSize, hashing))));
  }

  private static Arguments mapImplementation(
      String name, IntFunction<LongObjectMap<String>> factory) {
    return Arguments.of(name, factory);
  }

  private static Stream<LongHashing> hashings() {
    return Arrays.stream(LongHashing.values());
  }
}

package name.mrkandreev.mapsmith.openaddressing.strategies;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressMap;
import name.mrkandreev.mapsmith.ranking.OrderStatisticLongLongMap;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

@SuppressWarnings("PMD.TestClassWithoutTestCases") // jqwik properties are not recognized by PMD.
class LongLongMapPropertyTest {
  @Property
  void mapsMatchHashMapForGeneratedOperationSequences(
      @ForAll("operations") List<Operation> operations) {
    for (MapImplementation implementation : mapImplementations().toList()) {
      LongLongMap actual = implementation.factory().apply(1);
      Map<Long, Long> expected = new HashMap<>();

      for (Operation operation : operations) {
        if (operation instanceof Put put) {
          Long previous = expected.put(put.key(), put.value());
          assertThat(actual.put(put.key(), put.value()))
              .as(implementation.name())
              .isEqualTo(previous == null ? 0L : previous);
        } else if (operation instanceof Remove remove) {
          Long previous = expected.remove(remove.key());
          assertThat(actual.remove(remove.key()))
              .as(implementation.name())
              .isEqualTo(previous == null ? 0L : previous);
        } else {
          Get get = (Get) operation;
          assertThat(actual.containsKey(get.key()))
              .as(implementation.name())
              .isEqualTo(expected.containsKey(get.key()));
          assertThat(actual.getOrDefault(get.key(), Long.MIN_VALUE))
              .as(implementation.name())
              .isEqualTo(expected.getOrDefault(get.key(), Long.MIN_VALUE));
        }

        assertThat(actual.size()).as(implementation.name()).isEqualTo(expected.size());
      }
    }
  }

  @Provide
  Arbitrary<List<Operation>> operations() {
    Arbitrary<Long> keys = Arbitraries.longs().between(-100L, 100L);
    Arbitrary<Long> values = Arbitraries.longs();
    Arbitrary<Operation> puts = Combinators.combine(keys, values).as(Put::new);
    Arbitrary<Operation> removes = keys.map(Remove::new);
    Arbitrary<Operation> gets = keys.map(Get::new);

    return Arbitraries.oneOf(puts, removes, gets).list().ofMinSize(1).ofMaxSize(200);
  }

  private static Stream<MapImplementation> mapImplementations() {
    return Stream.of(LongHashing.values())
        .flatMap(
            hashing ->
                Stream.of(
                    new MapImplementation(
                        "linear probing " + hashing,
                        expectedSize -> new LinearProbingLongLongMap(expectedSize, hashing)),
                    new MapImplementation(
                        "robin hood " + hashing,
                        expectedSize -> new RobinHoodLongLongMap(expectedSize, hashing)),
                    new MapImplementation(
                        "swiss table " + hashing,
                        expectedSize -> new SwissTableLongLongMap(expectedSize, hashing)),
                    new MapImplementation(
                        "open address linear probing " + hashing,
                        expectedSize ->
                            new LongLongOpenAddressMap(
                                LinearProbingLongLongMap::new, expectedSize, hashing)),
                    new MapImplementation(
                        "open address robin hood " + hashing,
                        expectedSize ->
                            new LongLongOpenAddressMap(
                                RobinHoodLongLongMap::new, expectedSize, hashing)),
                    new MapImplementation(
                        "open address swiss table " + hashing,
                        expectedSize ->
                            new LongLongOpenAddressMap(
                                SwissTableLongLongMap::new, expectedSize, hashing)),
                    new MapImplementation(
                        "order statistic " + hashing, OrderStatisticLongLongMap::new)));
  }

  private sealed interface Operation permits Put, Remove, Get {}

  private record Put(long key, long value) implements Operation {}

  private record Remove(long key) implements Operation {}

  private record Get(long key) implements Operation {}

  private record MapImplementation(String name, IntFunction<LongLongMap> factory) {}
}

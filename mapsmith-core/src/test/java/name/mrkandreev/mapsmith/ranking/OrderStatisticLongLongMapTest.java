package name.mrkandreev.mapsmith.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class OrderStatisticLongLongMapTest {
  @Test
  void ranksEntriesByValueDescending() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();

    map.put(10L, 100L);
    map.put(20L, 300L);
    map.put(30L, 200L);

    assertThat(map.rankOf(20L)).isEqualTo(1);
    assertThat(map.rankOf(30L)).isEqualTo(2);
    assertThat(map.rankOf(10L)).isEqualTo(3);
    assertThat(map.countBefore(30L)).isEqualTo(1);
    assertThat(map.countAfter(30L)).isEqualTo(1);
  }

  @Test
  void usesKeyAsTieBreakerForEqualValues() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();

    map.put(30L, 100L);
    map.put(10L, 100L);
    map.put(20L, 100L);

    assertThat(map.rankOf(10L)).isEqualTo(1);
    assertThat(map.rankOf(20L)).isEqualTo(2);
    assertThat(map.rankOf(30L)).isEqualTo(3);
  }

  @Test
  void updatesRankWhenValueChanges() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();
    map.put(1L, 10L);
    map.put(2L, 20L);
    map.put(3L, 30L);

    assertThat(map.put(1L, 40L)).isEqualTo(10L);

    assertThat(map.rankOf(1L)).isEqualTo(1);
    assertThat(map.rankOf(3L)).isEqualTo(2);
    assertThat(map.rankOf(2L)).isEqualTo(3);
  }

  @Test
  void removesEntriesFromRanking() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();
    map.put(1L, 10L);
    map.put(2L, 20L);
    map.put(3L, 30L);

    assertThat(map.remove(3L)).isEqualTo(30L);

    assertThat(map.rankOf(2L)).isEqualTo(1);
    assertThat(map.countBefore(1L)).isEqualTo(1);
    assertThat(map.countAfter(1L)).isZero();
  }

  @Test
  void returnsMissingRankForAbsentKeys() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();

    assertThat(map.rankOf(404L)).isEqualTo(LongLongRankingMap.MISSING_RANK);
    assertThat(map.countBefore(404L)).isEqualTo(LongLongRankingMap.MISSING_RANK);
    assertThat(map.countAfter(404L)).isEqualTo(LongLongRankingMap.MISSING_RANK);
  }

  @Test
  void clearsRankedEntries() {
    LongLongRankingMap map = new OrderStatisticLongLongMap();
    map.put(1L, 10L);
    map.put(2L, 20L);

    map.clear();

    assertThat(map.isEmpty()).isTrue();
    assertThat(map.rankOf(1L)).isEqualTo(LongLongRankingMap.MISSING_RANK);
  }

  @Test
  void matchesExpectedRanksForRandomOperations() {
    LongLongRankingMap map = new OrderStatisticLongLongMap(1);
    Map<Long, Long> expected = new HashMap<>();
    SplittableRandom random = new SplittableRandom(0x6c656164657273L);

    for (int i = 0; i < 10_000; i++) {
      long key = random.nextLong(500L);
      long value = random.nextLong(1_000L);
      int operation = random.nextInt(4);

      if (operation == 0) {
        Long previousValue = expected.remove(key);
        assertThat(map.remove(key)).isEqualTo(previousValue == null ? 0L : previousValue);
      } else {
        Long previousValue = expected.put(key, value);
        assertThat(map.put(key, value)).isEqualTo(previousValue == null ? 0L : previousValue);
      }

      assertThat(map.size()).isEqualTo(expected.size());
      assertRanks(map, expected);
    }
  }

  private static void assertRanks(LongLongRankingMap map, Map<Long, Long> expected) {
    List<Map.Entry<Long, Long>> ordered = new ArrayList<>(expected.entrySet());
    ordered.sort(
        Comparator.<Map.Entry<Long, Long>>comparingLong(Map.Entry::getValue)
            .reversed()
            .thenComparingLong(Map.Entry::getKey));

    for (int index = 0; index < ordered.size(); index++) {
      long key = ordered.get(index).getKey();
      assertThat(map.rankOf(key)).isEqualTo(index + 1);
      assertThat(map.countBefore(key)).isEqualTo(index);
      assertThat(map.countAfter(key)).isEqualTo(ordered.size() - index - 1);
    }
  }
}

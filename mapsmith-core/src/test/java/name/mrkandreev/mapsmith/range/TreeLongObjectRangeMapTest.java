package name.mrkandreev.mapsmith.range;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class TreeLongObjectRangeMapTest {
  private static final String SAME_VALUE = "same";
  private static final String STORED_VALUE = "value";

  @Test
  void startsEmpty() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    assertThat(map.size()).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(10L)).isNull();
    assertThat(map.getOrDefault(10L, "fallback")).isEqualTo("fallback");
  }

  @Test
  void storesAndFindsRanges() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    map.put(10L, 20L, "tier-a");

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.containsKey(9L)).isFalse();
    assertThat(map.containsKey(10L)).isTrue();
    assertThat(map.containsKey(20L)).isTrue();
    assertThat(map.containsKey(21L)).isFalse();
    assertThat(map.get(15L)).isEqualTo("tier-a");
  }

  @Test
  void rangePutOverwritesAndSplitsOverlaps() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();
    map.put(0L, 9L, "left");
    map.put(20L, 29L, "right");

    map.put(5L, 24L, "middle");

    assertThat(map.size()).isEqualTo(3);
    assertThat(map.get(4L)).isEqualTo("left");
    assertThat(map.get(5L)).isEqualTo("middle");
    assertThat(map.get(24L)).isEqualTo("middle");
    assertThat(map.get(25L)).isEqualTo("right");
  }

  @Test
  void putCoalescingMergesAdjacentRangesWithEqualValues() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    map.putCoalescing(0L, 9L, sameValueCopy());
    map.putCoalescing(10L, 19L, sameValueCopy());
    map.putCoalescing(20L, 29L, SAME_VALUE);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(0L)).isEqualTo(SAME_VALUE);
    assertThat(map.get(29L)).isEqualTo(SAME_VALUE);
  }

  @Test
  void supportsNullValues() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    map.put(0L, 9L, null);

    assertThat(map.containsKey(5L)).isTrue();
    assertThat(map.get(5L)).isNull();
    assertThat(map.getOrDefault(5L, "fallback")).isNull();
  }

  @Test
  void removesSingleKeysAndRanges() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();
    map.put(0L, 99L, STORED_VALUE);

    map.remove(50L, 50L);
    map.remove(20L, 29L);

    assertThat(map.get(19L)).isEqualTo(STORED_VALUE);
    assertThat(map.containsKey(20L)).isFalse();
    assertThat(map.containsKey(29L)).isFalse();
    assertThat(map.get(30L)).isEqualTo(STORED_VALUE);
    assertThat(map.containsKey(50L)).isFalse();
    assertThat(map.get(51L)).isEqualTo(STORED_VALUE);
  }

  @Test
  void supportsOpenClosedAndUnboundedRanges() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    map.put(10L, LongBoundType.OPEN, 20L, LongBoundType.CLOSED, "a");
    map.put(Long.MIN_VALUE, LongBoundType.CLOSED, 0L, LongBoundType.OPEN, "b");
    map.put(100L, LongBoundType.OPEN, Long.MAX_VALUE, LongBoundType.CLOSED, "c");

    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(11L)).isEqualTo("a");
    assertThat(map.get(20L)).isEqualTo("a");
    assertThat(map.get(-1L)).isEqualTo("b");
    assertThat(map.containsKey(0L)).isFalse();
    assertThat(map.containsKey(100L)).isFalse();
    assertThat(map.get(101L)).isEqualTo("c");
  }

  @Test
  void rejectsInvalidRangesAndNullBoundTypes() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();

    assertThatThrownBy(() -> map.put(2L, 1L, STORED_VALUE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fromInclusive must be <= toInclusive");
    assertThatThrownBy(() -> map.remove(2L, 1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fromInclusive must be <= toInclusive");
    assertThatThrownBy(() -> map.put(1L, null, 2L, LongBoundType.CLOSED, STORED_VALUE))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("lowerType must not be null");
    assertThatThrownBy(() -> map.putCoalescing(1L, LongBoundType.CLOSED, 2L, null, STORED_VALUE))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("upperType must not be null");
    assertThatThrownBy(() -> map.remove(1L, LongBoundType.OPEN, 1L, LongBoundType.OPEN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("open range endpoints must be different");
  }

  @Test
  void matchesPointModelForRandomOperations() {
    LongObjectRangeMap<String> map = new TreeLongObjectRangeMap<>();
    Map<Long, String> expected = new HashMap<>();
    SplittableRandom random = new SplittableRandom(0x72616e67656d6170L);

    for (int i = 0; i < 10_000; i++) {
      long from = random.nextLong(-50L, 51L);
      long to = random.nextLong(from, 51L);
      int operation = random.nextInt(3);

      if (operation == 0) {
        map.remove(from, to);
        for (long key = from; key <= to; key++) {
          expected.remove(key);
        }
      } else {
        String value = STORED_VALUE + "-" + random.nextLong();
        map.put(from, to, value);
        for (long key = from; key <= to; key++) {
          expected.put(key, value);
        }
      }

      for (long key = -50L; key <= 50L; key++) {
        assertThat(map.containsKey(key)).isEqualTo(expected.containsKey(key));
        assertThat(map.getOrDefault(key, "missing"))
            .isEqualTo(expected.getOrDefault(key, "missing"));
      }
    }
  }

  private static String sameValueCopy() {
    return SAME_VALUE.substring(0, 2) + SAME_VALUE.substring(2);
  }
}

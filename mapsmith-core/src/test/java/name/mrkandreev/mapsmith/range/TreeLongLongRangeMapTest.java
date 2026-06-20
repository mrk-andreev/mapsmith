package name.mrkandreev.mapsmith.range;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class TreeLongLongRangeMapTest {
  @Test
  void startsEmpty() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    assertThat(map.size()).isZero();
    assertThat(map.isEmpty()).isTrue();
    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(10L)).isZero();
    assertThat(map.getOrDefault(10L, 42L)).isEqualTo(42L);
  }

  @Test
  void storesAndFindsRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(10L, 20L, 100L);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.containsKey(9L)).isFalse();
    assertThat(map.containsKey(10L)).isTrue();
    assertThat(map.containsKey(20L)).isTrue();
    assertThat(map.containsKey(21L)).isFalse();
    assertThat(map.get(15L)).isEqualTo(100L);
  }

  @Test
  void singletonRangeOverwritesSingleKey() {
    LongLongRangeMap map = new TreeLongLongRangeMap();
    map.put(10L, 20L, 100L);

    map.put(15L, 15L, 200L);

    assertThat(map.size()).isEqualTo(3);
    assertThat(map.get(14L)).isEqualTo(100L);
    assertThat(map.get(15L)).isEqualTo(200L);
    assertThat(map.get(16L)).isEqualTo(100L);
  }

  @Test
  void rangePutOverwritesAndSplitsOverlaps() {
    LongLongRangeMap map = new TreeLongLongRangeMap();
    map.put(0L, 9L, 10L);
    map.put(20L, 29L, 20L);

    map.put(5L, 24L, 99L);

    assertThat(map.size()).isEqualTo(3);
    assertThat(map.get(4L)).isEqualTo(10L);
    assertThat(map.get(5L)).isEqualTo(99L);
    assertThat(map.get(24L)).isEqualTo(99L);
    assertThat(map.get(25L)).isEqualTo(20L);
  }

  @Test
  void putDoesNotCoalesceAdjacentRangesWithSameValue() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(0L, 9L, 10L);
    map.put(10L, 19L, 10L);
    map.put(20L, 29L, 10L);

    assertThat(map.size()).isEqualTo(3);
    assertThat(map.get(0L)).isEqualTo(10L);
    assertThat(map.get(29L)).isEqualTo(10L);
  }

  @Test
  void putCoalescingMergesAdjacentRangesWithSameValue() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(0L, 9L, 10L);
    map.putCoalescing(10L, 19L, 10L);
    map.putCoalescing(20L, 29L, 10L);

    assertThat(map.size()).isEqualTo(1);
    assertThat(map.get(0L)).isEqualTo(10L);
    assertThat(map.get(29L)).isEqualTo(10L);
  }

  @Test
  void removesSingleKeysAndRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();
    map.put(0L, 99L, 10L);

    map.remove(50L, 50L);
    map.remove(20L, 29L);

    assertThat(map.get(19L)).isEqualTo(10L);
    assertThat(map.containsKey(20L)).isFalse();
    assertThat(map.containsKey(29L)).isFalse();
    assertThat(map.get(30L)).isEqualTo(10L);
    assertThat(map.containsKey(50L)).isFalse();
    assertThat(map.get(51L)).isEqualTo(10L);
  }

  @Test
  void supportsLongExtremes() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(Long.MIN_VALUE, Long.MIN_VALUE, 1L);
    map.put(Long.MAX_VALUE, Long.MAX_VALUE, 2L);

    assertThat(map.get(Long.MIN_VALUE)).isEqualTo(1L);
    assertThat(map.get(Long.MAX_VALUE)).isEqualTo(2L);
  }

  @Test
  void supportsOpenClosedAndUnboundedRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(10L, LongBoundType.OPEN, 20L, LongBoundType.CLOSED, 1L);
    map.put(Long.MIN_VALUE, LongBoundType.CLOSED, 0L, LongBoundType.OPEN, 2L);
    map.put(100L, LongBoundType.OPEN, Long.MAX_VALUE, LongBoundType.CLOSED, 3L);

    assertThat(map.containsKey(10L)).isFalse();
    assertThat(map.get(11L)).isEqualTo(1L);
    assertThat(map.get(20L)).isEqualTo(1L);
    assertThat(map.get(-1L)).isEqualTo(2L);
    assertThat(map.containsKey(0L)).isFalse();
    assertThat(map.containsKey(100L)).isFalse();
    assertThat(map.get(101L)).isEqualTo(3L);
  }

  @Test
  void ignoresEmptyDiscreteRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(10L, LongBoundType.CLOSED, 10L, LongBoundType.OPEN, 1L);
    map.remove(10L, LongBoundType.CLOSED, 10L, LongBoundType.OPEN);

    assertThat(map.isEmpty()).isTrue();
  }

  @Test
  void doesNotCoalesceAdjacentRangesWithDifferentValues() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(0L, 0L, 1L);
    map.putCoalescing(2L, 2L, 2L);
    map.putCoalescing(1L, 1L, 1L);
    assertThat(map.size()).isEqualTo(2);
  }

  @Test
  void coalescesRangesAtMaximumLongValue() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(Long.MAX_VALUE - 1L, Long.MAX_VALUE, 3L);
    map.putCoalescing(Long.MAX_VALUE, Long.MAX_VALUE, 3L);
    assertThat(map.get(Long.MAX_VALUE)).isEqualTo(3L);
  }

  @Test
  void ignoresEmptyTypedRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(Long.MAX_VALUE, LongBoundType.OPEN, Long.MAX_VALUE, LongBoundType.CLOSED, 4L);
    map.putCoalescing(Long.MIN_VALUE, LongBoundType.CLOSED, Long.MIN_VALUE, LongBoundType.OPEN, 4L);
    map.remove(Long.MAX_VALUE, LongBoundType.OPEN, Long.MAX_VALUE, LongBoundType.CLOSED);
    map.remove(Long.MIN_VALUE, LongBoundType.CLOSED, Long.MIN_VALUE, LongBoundType.OPEN);
    assertThat(map.isEmpty()).isTrue();
  }

  @Test
  void clearsRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.put(0L, 1L, 1L);
    map.clear();
    assertThat(map.isEmpty()).isTrue();
  }

  @Test
  void coalescesForwardAndRemovesTypedRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(2L, 2L, 1L);
    map.putCoalescing(0L, 1L, 1L);
    assertThat(map.size()).isEqualTo(1);
    map.remove(0L, LongBoundType.OPEN, 2L, LongBoundType.OPEN);
    assertThat(map.get(0L)).isEqualTo(1L);
    assertThat(map.get(1L)).isZero();
    assertThat(map.get(2L)).isEqualTo(1L);
    assertThat(map.isEmpty()).isFalse();
  }

  @Test
  void handlesNonTouchingEqualRangesAndEmptyTypedRemoval() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    map.putCoalescing(0L, 0L, 1L);
    map.putCoalescing(2L, 2L, 1L);
    assertThat(map.size()).isEqualTo(2);
    map.clear();
    map.putCoalescing(2L, 2L, 1L);
    map.putCoalescing(0L, 0L, 1L);
    assertThat(map.size()).isEqualTo(2);
    map.remove(Long.MIN_VALUE, LongBoundType.CLOSED, Long.MIN_VALUE, LongBoundType.OPEN);
    map.remove(0L, LongBoundType.CLOSED, 2L, LongBoundType.CLOSED);
    map.remove(0L, LongBoundType.CLOSED, 2L, LongBoundType.OPEN);
    map.remove(1L, LongBoundType.CLOSED, 1L, LongBoundType.OPEN);
  }

  @Test
  void rejectsInvalidClosedRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    assertThatThrownBy(() -> map.put(2L, 1L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fromInclusive must be <= toInclusive");
    assertThatThrownBy(() -> map.remove(2L, 1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fromInclusive must be <= toInclusive");
  }

  @Test
  void rejectsNullBoundTypes() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    assertThatThrownBy(() -> map.put(1L, null, 2L, LongBoundType.CLOSED, 10L))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("lowerType must not be null");
    assertThatThrownBy(() -> map.putCoalescing(1L, LongBoundType.CLOSED, 2L, null, 10L))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("upperType must not be null");
    assertThatThrownBy(() -> map.remove(1L, null, 2L, LongBoundType.CLOSED))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("lowerType must not be null");
  }

  @Test
  void rejectsInvalidTypedRanges() {
    LongLongRangeMap map = new TreeLongLongRangeMap();

    assertThatThrownBy(() -> map.remove(1L, LongBoundType.OPEN, 1L, LongBoundType.OPEN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("open range endpoints must be different");
    assertThatThrownBy(() -> map.remove(2L, LongBoundType.CLOSED, 1L, LongBoundType.CLOSED))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("lower must be <= upper");
  }

  @Test
  void matchesPointModelForRandomOperations() {
    LongLongRangeMap map = new TreeLongLongRangeMap();
    Map<Long, Long> expected = new HashMap<>();
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
        long value = random.nextLong();
        map.put(from, to, value);
        for (long key = from; key <= to; key++) {
          expected.put(key, value);
        }
      }

      for (long key = -50L; key <= 50L; key++) {
        assertThat(map.containsKey(key)).isEqualTo(expected.containsKey(key));
        assertThat(map.getOrDefault(key, Long.MIN_VALUE))
            .isEqualTo(expected.getOrDefault(key, Long.MIN_VALUE));
      }
    }
  }
}

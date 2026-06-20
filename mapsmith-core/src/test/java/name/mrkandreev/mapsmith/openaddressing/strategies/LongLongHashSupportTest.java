package name.mrkandreev.mapsmith.openaddressing.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LongLongHashSupportTest {
  @Test
  void returnsMinimumCapacityForZeroExpectedSize() {
    assertThat(LongLongHashSupport.capacityFor(0)).isEqualTo(2);
  }

  @Test
  void returnsMinimumCapacityForOneExpectedEntry() {
    assertThat(LongLongHashSupport.capacityFor(1)).isEqualTo(2);
  }

  @Test
  void rejectsNegativeExpectedSize() {
    assertThatThrownBy(() -> LongLongHashSupport.capacityFor(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize must be non-negative");
  }

  @Test
  void rejectsExpectedSizeAboveMaximumCapacity() {
    assertThatThrownBy(() -> LongLongHashSupport.capacityFor(Integer.MAX_VALUE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("expectedSize is too large");
  }

  @Test
  void doublesCapacityBelowMaximum() {
    assertThat(LongLongHashSupport.nextCapacity(2)).isEqualTo(4);
  }

  @Test
  void rejectsCapacityGrowthAtMaximum() {
    assertThatThrownBy(() -> LongLongHashSupport.nextCapacity(1 << 30))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("maximum capacity reached");
  }
}

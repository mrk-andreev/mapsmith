package name.mrkandreev.mapsmith.openaddressing;

import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.RobinHoodLongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.SwissTableLongObjectMap;

/** Creates primitive long-to-object open-addressing maps. */
@FunctionalInterface
public interface LongObjectOpenAddressingStrategy<T> {
  /** Linear-probing strategy. */
  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy LINEAR_PROBING = LinearProbingLongObjectMap::new;

  /** Robin Hood hashing strategy. */
  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy ROBIN_HOOD = RobinHoodLongObjectMap::new;

  /** Swiss-table strategy. */
  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy SWISS_TABLE = SwissTableLongObjectMap::new;

  /**
   * Creates a map.
   *
   * @param expectedSize expected entry count
   * @param hashing hash function
   * @return new map
   */
  LongObjectMap<T> create(int expectedSize, LongHashing hashing);
}

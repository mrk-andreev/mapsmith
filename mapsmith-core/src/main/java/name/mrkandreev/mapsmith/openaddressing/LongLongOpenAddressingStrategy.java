package name.mrkandreev.mapsmith.openaddressing;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.RobinHoodLongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.SwissTableLongLongMap;

/** Creates primitive long-to-long open-addressing maps. */
@FunctionalInterface
public interface LongLongOpenAddressingStrategy {
  /** Linear-probing strategy. */
  LongLongOpenAddressingStrategy LINEAR_PROBING = LinearProbingLongLongMap::new;

  /** Robin Hood hashing strategy. */
  LongLongOpenAddressingStrategy ROBIN_HOOD = RobinHoodLongLongMap::new;

  /** Swiss-table strategy. */
  LongLongOpenAddressingStrategy SWISS_TABLE = SwissTableLongLongMap::new;

  /**
   * Creates a map.
   *
   * @param expectedSize expected entry count
   * @param hashing hash function
   * @return new map
   */
  LongLongMap create(int expectedSize, LongHashing hashing);
}

package name.mrkandreev.mapsmith.openaddressing;

import name.mrkandreev.mapsmith.LongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.RobinHoodLongObjectMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.SwissTableLongObjectMap;

@FunctionalInterface
public interface LongObjectOpenAddressingStrategy<T> {
  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy LINEAR_PROBING = LinearProbingLongObjectMap::new;

  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy ROBIN_HOOD = RobinHoodLongObjectMap::new;

  @SuppressWarnings("rawtypes")
  LongObjectOpenAddressingStrategy SWISS_TABLE = SwissTableLongObjectMap::new;

  LongObjectMap<T> create(int expectedSize, LongHashing hashing);
}

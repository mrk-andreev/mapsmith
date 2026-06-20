package name.mrkandreev.mapsmith.openaddressing;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.LinearProbingLongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.RobinHoodLongLongMap;
import name.mrkandreev.mapsmith.openaddressing.strategies.SwissTableLongLongMap;

@FunctionalInterface
public interface LongLongOpenAddressingStrategy {
  LongLongOpenAddressingStrategy LINEAR_PROBING = LinearProbingLongLongMap::new;
  LongLongOpenAddressingStrategy ROBIN_HOOD = RobinHoodLongLongMap::new;
  LongLongOpenAddressingStrategy SWISS_TABLE = SwissTableLongLongMap::new;

  LongLongMap create(int expectedSize, LongHashing hashing);
}

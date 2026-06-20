package name.mrkandreev.mapsmith.samples;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressMap;
import name.mrkandreev.mapsmith.openaddressing.LongLongOpenAddressingStrategy;

public enum CustomOpenAddressStrategyExample {
  ;

  public static void main(String[] args) {
    LongLongOpenAddressingStrategy strategy = LongLongOpenAddressingStrategy.ROBIN_HOOD;
    LongLongMap counters = new LongLongOpenAddressMap(strategy, 64, LongHashing.MURMUR3_FINALIZER);

    counters.put(42L, counters.get(42L) + 1L);
    counters.put(42L, counters.get(42L) + 1L);

    System.out.println("Custom open-addressing strategy");
    System.out.printf("event 42 count = %d%n", counters.get(42L));
  }
}

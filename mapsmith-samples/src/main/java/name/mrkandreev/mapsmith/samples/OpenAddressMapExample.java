package name.mrkandreev.mapsmith.samples;

import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongLongMapFactory;
import name.mrkandreev.mapsmith.openaddressing.MapSpecialization;

public enum OpenAddressMapExample {
  ;

  public static void main(String[] args) {
    LongLongMap balances =
        LongLongMapFactory.create(MapSpecialization.SWISS_TABLE, 1_000, LongHashing.FIBONACCI);

    balances.put(101L, 2_500L);
    balances.put(102L, 7_000L);
    balances.put(101L, 2_750L);

    System.out.println("Open address map");
    System.out.printf("user 101 balance = %d%n", balances.get(101L));
    System.out.printf("user 102 exists = %b%n", balances.containsKey(102L));
  }
}

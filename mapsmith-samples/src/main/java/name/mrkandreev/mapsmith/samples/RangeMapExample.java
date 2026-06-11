package name.mrkandreev.mapsmith.samples;

import name.mrkandreev.mapsmith.range.LongBoundType;
import name.mrkandreev.mapsmith.range.LongLongRangeMap;
import name.mrkandreev.mapsmith.range.TreeLongLongRangeMap;

public enum RangeMapExample {
  ;

  public static void main(String[] args) {
    LongLongRangeMap tiers = new TreeLongLongRangeMap();

    tiers.put(0L, 999L, 1L);
    tiers.put(1_000L, 4_999L, 2L);
    tiers.put(5_000L, LongBoundType.CLOSED, 10_000L, LongBoundType.OPEN, 3L);

    System.out.println("Range map");
    System.out.printf("score 750 tier = %d%n", tiers.get(750L));
    System.out.printf("score 2500 tier = %d%n", tiers.get(2_500L));
    System.out.printf("score 10000 tier exists = %b%n", tiers.containsKey(10_000L));

    tiers.remove(900L, 1_100L);
    System.out.printf("score 950 tier after exclusion = %d%n", tiers.getOrDefault(950L, -1L));
  }
}

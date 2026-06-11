package name.mrkandreev.mapsmith.ranking;

import name.mrkandreev.mapsmith.LongLongMap;

public interface LongLongRankingMap extends LongLongMap {
  int MISSING_RANK = -1;

  /**
   * Returns a one-based rank for {@code key}. Higher values are ranked first. Keys with equal
   * values are ranked by key in ascending order.
   */
  int rankOf(long key);

  /**
   * Returns how many entries are ranked before {@code key}, or {@link #MISSING_RANK} when the key
   * is not present.
   */
  int countBefore(long key);

  /**
   * Returns how many entries are ranked after {@code key}, or {@link #MISSING_RANK} when the key is
   * not present.
   */
  int countAfter(long key);
}

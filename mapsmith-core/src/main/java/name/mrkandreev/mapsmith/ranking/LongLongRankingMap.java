package name.mrkandreev.mapsmith.ranking;

import name.mrkandreev.mapsmith.LongLongMap;

/** A long-to-long map that exposes the rank of each entry. */
public interface LongLongRankingMap extends LongLongMap {
  /** Rank returned for absent keys. */
  int MISSING_RANK = -1;

  /**
   * Returns a one-based rank for {@code key}. Higher values are ranked first. Keys with equal
   * values are ranked by key in ascending order.
   *
   * @param key key to rank
   * @return one-based rank, or {@link #MISSING_RANK} when absent
   */
  int rankOf(long key);

  /**
   * Returns how many entries are ranked before {@code key}, or {@link #MISSING_RANK} when the key
   * is not present.
   *
   * @param key key to inspect
   * @return number of preceding entries, or {@link #MISSING_RANK} when absent
   */
  int countBefore(long key);

  /**
   * Returns how many entries are ranked after {@code key}, or {@link #MISSING_RANK} when the key is
   * not present.
   *
   * @param key key to inspect
   * @return number of following entries, or {@link #MISSING_RANK} when absent
   */
  int countAfter(long key);
}

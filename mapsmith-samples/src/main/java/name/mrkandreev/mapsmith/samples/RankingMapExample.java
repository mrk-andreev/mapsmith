package name.mrkandreev.mapsmith.samples;

import name.mrkandreev.mapsmith.ranking.LongLongRankingMap;
import name.mrkandreev.mapsmith.ranking.OrderStatisticLongLongMap;

public enum RankingMapExample {
  ;

  public static void main(String[] args) {
    LongLongRankingMap leaderboard = new OrderStatisticLongLongMap();

    leaderboard.put(10L, 1_200L);
    leaderboard.put(20L, 3_400L);
    leaderboard.put(30L, 2_100L);
    leaderboard.put(40L, 3_400L);

    System.out.println("Ranking map");
    System.out.printf("user 20 rank = %d%n", leaderboard.rankOf(20L));
    System.out.printf("user 30 rank = %d%n", leaderboard.rankOf(30L));
    System.out.printf("entries before user 30 = %d%n", leaderboard.countBefore(30L));
    System.out.printf("entries after user 30 = %d%n", leaderboard.countAfter(30L));
  }
}

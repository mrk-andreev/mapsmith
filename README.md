# mapsmith

[![CI](https://github.com/mrk-andreev/mapsmith/actions/workflows/ci.yml/badge.svg)](https://github.com/mrk-andreev/mapsmith/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

High-performance primitive map implementations for Java.

mapsmith focuses on `long`-keyed data structures that avoid key boxing, keep APIs small, and make
specialized access patterns explicit. It currently includes open-addressed hash maps, range maps,
and ranking maps.

## What's inside

- `LongLongMap`: a compact primitive map interface for `long` keys and `long` values.
- `LongObjectMap<T>`: a compact map interface for primitive `long` keys and generic object values.
- Open-addressed maps with linear probing, Robin Hood hashing, and SwissTable-style probing.
- Pluggable long hash functions: Murmur3 finalizer, Fibonacci hashing, XOR shift, and identity.
- `LongLongRangeMap`: stores values over long ranges with closed or open bounds.
- `LongObjectRangeMap<T>`: stores object values over long ranges.
- `LongLongRankingMap`: tracks values and returns leaderboard-style ranks.
- JMH benchmarks for comparing map implementations and tuning tradeoffs.

## Requirements

- Java 21+
- Gradle wrapper included in the repository

## Install

The core library is published as:

```kotlin
dependencies {
  implementation("name.mrkandreev:mapsmith-core:<version>")
}
```

For local development, use the included Gradle wrapper:

```bash
./gradlew check
```

## Quick start

Create a primitive hash map with a chosen strategy and hashing function:

```java
import name.mrkandreev.mapsmith.LongLongMap;
import name.mrkandreev.mapsmith.openaddressing.LongHashing;
import name.mrkandreev.mapsmith.openaddressing.LongLongMapFactory;
import name.mrkandreev.mapsmith.openaddressing.MapSpecialization;

LongLongMap balances =
    LongLongMapFactory.create(MapSpecialization.SWISS_TABLE, 1_000, LongHashing.FIBONACCI);

balances.put(101L, 2_500L);
balances.put(102L, 7_000L);

long balance = balances.getOrDefault(101L, 0L);
boolean exists = balances.containsKey(102L);
```

Use a range map when values apply to spans of keys:

```java
import name.mrkandreev.mapsmith.range.LongBoundType;
import name.mrkandreev.mapsmith.range.LongLongRangeMap;
import name.mrkandreev.mapsmith.range.TreeLongLongRangeMap;

LongLongRangeMap tiers = new TreeLongLongRangeMap();

tiers.put(0L, 999L, 1L);
tiers.put(1_000L, 4_999L, 2L);
tiers.put(5_000L, LongBoundType.CLOSED, 10_000L, LongBoundType.OPEN, 3L);

long tier = tiers.getOrDefault(2_500L, -1L);
```

Use a ranking map for leaderboard-style ordering. Higher values rank first; equal values are ordered
by key ascending.

```java
import name.mrkandreev.mapsmith.ranking.LongLongRankingMap;
import name.mrkandreev.mapsmith.ranking.OrderStatisticLongLongMap;

LongLongRankingMap leaderboard = new OrderStatisticLongLongMap();

leaderboard.put(10L, 1_200L);
leaderboard.put(20L, 3_400L);
leaderboard.put(30L, 2_100L);

int rank = leaderboard.rankOf(20L);
int entriesAfter = leaderboard.countAfter(20L);
```

## Modules

- `mapsmith-core`: library code and tests.
- `mapsmith-samples`: runnable examples for open-addressed maps, custom strategies, range maps, and
  ranking maps.
- `mapsmith-benchmarks`: JMH benchmark suite.

Run the sample app:

```bash
./gradlew :mapsmith-samples:run
```

## Benchmarks

Run all JMH benchmarks:

```bash
./gradlew :mapsmith-benchmarks:jmh
```

Run one benchmark class:

```bash
./gradlew :mapsmith-benchmarks:jmh --args='LongLongMapBenchmark'
```

Run object-value benchmark classes separately:

```bash
./gradlew :mapsmith-benchmarks:jmh --args='LongObjectMapBenchmark|LongObjectRangeMapBenchmark'
```

Recent local results on an Apple M4 Pro with OpenJDK 25 show the primitive open-addressed maps
outperforming `java.util.HashMap<Long, Long>` in the included `getExisting` and `putAll`
benchmarks. The exact winner depends on workload, size, hashing function, and collision profile, so
rerun the JMH suite on your own hardware before making performance-sensitive choices. The captured
benchmark output is available in [mapsmith-benchmarks/results.md](mapsmith-benchmarks/results.md).

## Development

Useful commands:

```bash
./gradlew check
./gradlew spotlessApply
./gradlew :mapsmith-core:test
./gradlew :mapsmith-benchmarks:jmh --args='LongLongRangeMapBenchmark'
```

The build uses Java 21 toolchains, JUnit, AssertJ, Spotless, PMD, SpotBugs, Error Prone, JaCoCo,
and JMH.

## License

MIT. See [LICENSE](LICENSE).

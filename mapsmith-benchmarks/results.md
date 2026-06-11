# Benchmark results

Run the `LongLongMapBenchmark` suite with:

```bash
./gradlew :mapsmith-benchmarks:jmh --args='LongLongMapBenchmark'
```

The following results were captured on:

- OS: macOS 26.5.1 (Darwin 25.5.0), arm64
- Hardware: MacBook Pro (Mac16,8), Apple M4 Pro
- CPU cores: 12 total (8 performance, 4 efficiency)
- Memory: 48 GB
- JVM: OpenJDK 25 (25+36-3489)
- Gradle: 9.3.0

```text
Benchmark                                                (mapKind)  (size)   Mode  Cnt       Score       Error  Units
LongLongMapBenchmark.getExisting  LINEAR_PROBING_MURMUR3_FINALIZER    1000  thrpt   10  647570.987 ±  7922.800  ops/s
LongLongMapBenchmark.getExisting  LINEAR_PROBING_MURMUR3_FINALIZER  100000  thrpt   10    4091.284 ±   346.016  ops/s
LongLongMapBenchmark.getExisting          LINEAR_PROBING_FIBONACCI    1000  thrpt   10  816294.643 ± 12402.125  ops/s
LongLongMapBenchmark.getExisting          LINEAR_PROBING_FIBONACCI  100000  thrpt   10    4345.009 ±   608.994  ops/s
LongLongMapBenchmark.getExisting          LINEAR_PROBING_XOR_SHIFT    1000  thrpt   10  694567.270 ±  8330.025  ops/s
LongLongMapBenchmark.getExisting          LINEAR_PROBING_XOR_SHIFT  100000  thrpt   10    4331.375 ±   586.359  ops/s
LongLongMapBenchmark.getExisting           LINEAR_PROBING_IDENTITY    1000  thrpt   10  842079.161 ±  8063.239  ops/s
LongLongMapBenchmark.getExisting           LINEAR_PROBING_IDENTITY  100000  thrpt   10    4714.343 ±   319.401  ops/s
LongLongMapBenchmark.getExisting      ROBIN_HOOD_MURMUR3_FINALIZER    1000  thrpt   10  578332.739 ±  3500.212  ops/s
LongLongMapBenchmark.getExisting      ROBIN_HOOD_MURMUR3_FINALIZER  100000  thrpt   10    3208.451 ±   154.616  ops/s
LongLongMapBenchmark.getExisting              ROBIN_HOOD_FIBONACCI    1000  thrpt   10  782382.398 ±  6103.763  ops/s
LongLongMapBenchmark.getExisting              ROBIN_HOOD_FIBONACCI  100000  thrpt   10    4265.135 ±   442.401  ops/s
LongLongMapBenchmark.getExisting              ROBIN_HOOD_XOR_SHIFT    1000  thrpt   10  650678.108 ±  2764.606  ops/s
LongLongMapBenchmark.getExisting              ROBIN_HOOD_XOR_SHIFT  100000  thrpt   10    4209.408 ±   291.070  ops/s
LongLongMapBenchmark.getExisting               ROBIN_HOOD_IDENTITY    1000  thrpt   10  942967.963 ±  6203.312  ops/s
LongLongMapBenchmark.getExisting               ROBIN_HOOD_IDENTITY  100000  thrpt   10    4685.244 ±   462.282  ops/s
LongLongMapBenchmark.getExisting     SWISS_TABLE_MURMUR3_FINALIZER    1000  thrpt   10  614440.865 ±  2317.823  ops/s
LongLongMapBenchmark.getExisting     SWISS_TABLE_MURMUR3_FINALIZER  100000  thrpt   10    3898.600 ±   384.452  ops/s
LongLongMapBenchmark.getExisting             SWISS_TABLE_FIBONACCI    1000  thrpt   10  789379.420 ±  3260.503  ops/s
LongLongMapBenchmark.getExisting             SWISS_TABLE_FIBONACCI  100000  thrpt   10    4641.703 ±   244.405  ops/s
LongLongMapBenchmark.getExisting             SWISS_TABLE_XOR_SHIFT    1000  thrpt   10  655457.131 ± 11165.597  ops/s
LongLongMapBenchmark.getExisting             SWISS_TABLE_XOR_SHIFT  100000  thrpt   10    4403.082 ±   326.740  ops/s
LongLongMapBenchmark.getExisting              SWISS_TABLE_IDENTITY    1000  thrpt   10  821909.278 ±  7632.234  ops/s
LongLongMapBenchmark.getExisting              SWISS_TABLE_IDENTITY  100000  thrpt   10    4836.804 ±   248.294  ops/s
LongLongMapBenchmark.getExisting                          HASH_MAP    1000  thrpt   10  444449.594 ± 16838.833  ops/s
LongLongMapBenchmark.getExisting                          HASH_MAP  100000  thrpt   10    1999.496 ±    36.114  ops/s
LongLongMapBenchmark.putAll       LINEAR_PROBING_MURMUR3_FINALIZER    1000  thrpt   10  388122.930 ±  3063.760  ops/s
LongLongMapBenchmark.putAll       LINEAR_PROBING_MURMUR3_FINALIZER  100000  thrpt   10    3267.017 ±    45.871  ops/s
LongLongMapBenchmark.putAll               LINEAR_PROBING_FIBONACCI    1000  thrpt   10  447810.987 ±  2366.250  ops/s
LongLongMapBenchmark.putAll               LINEAR_PROBING_FIBONACCI  100000  thrpt   10    3266.107 ±   129.783  ops/s
LongLongMapBenchmark.putAll               LINEAR_PROBING_XOR_SHIFT    1000  thrpt   10  408573.203 ±  3688.601  ops/s
LongLongMapBenchmark.putAll               LINEAR_PROBING_XOR_SHIFT  100000  thrpt   10    3332.231 ±    59.894  ops/s
LongLongMapBenchmark.putAll                LINEAR_PROBING_IDENTITY    1000  thrpt   10  467259.529 ±  3640.637  ops/s
LongLongMapBenchmark.putAll                LINEAR_PROBING_IDENTITY  100000  thrpt   10    3536.897 ±    39.149  ops/s
LongLongMapBenchmark.putAll           ROBIN_HOOD_MURMUR3_FINALIZER    1000  thrpt   10  292037.559 ±  1266.328  ops/s
LongLongMapBenchmark.putAll           ROBIN_HOOD_MURMUR3_FINALIZER  100000  thrpt   10    2208.261 ±   203.691  ops/s
LongLongMapBenchmark.putAll                   ROBIN_HOOD_FIBONACCI    1000  thrpt   10  302963.038 ±  2247.891  ops/s
LongLongMapBenchmark.putAll                   ROBIN_HOOD_FIBONACCI  100000  thrpt   10    2145.929 ±   216.520  ops/s
LongLongMapBenchmark.putAll                   ROBIN_HOOD_XOR_SHIFT    1000  thrpt   10  294839.314 ±   581.408  ops/s
LongLongMapBenchmark.putAll                   ROBIN_HOOD_XOR_SHIFT  100000  thrpt   10    2164.843 ±    80.350  ops/s
LongLongMapBenchmark.putAll                    ROBIN_HOOD_IDENTITY    1000  thrpt   10  304588.688 ±  1365.613  ops/s
LongLongMapBenchmark.putAll                    ROBIN_HOOD_IDENTITY  100000  thrpt   10    2333.448 ±    92.305  ops/s
LongLongMapBenchmark.putAll          SWISS_TABLE_MURMUR3_FINALIZER    1000  thrpt   10  375817.768 ±  3421.492  ops/s
LongLongMapBenchmark.putAll          SWISS_TABLE_MURMUR3_FINALIZER  100000  thrpt   10    3035.654 ±   322.720  ops/s
LongLongMapBenchmark.putAll                  SWISS_TABLE_FIBONACCI    1000  thrpt   10  439015.150 ±  8078.347  ops/s
LongLongMapBenchmark.putAll                  SWISS_TABLE_FIBONACCI  100000  thrpt   10    3169.778 ±   131.227  ops/s
LongLongMapBenchmark.putAll                  SWISS_TABLE_XOR_SHIFT    1000  thrpt   10  404543.853 ±   634.809  ops/s
LongLongMapBenchmark.putAll                  SWISS_TABLE_XOR_SHIFT  100000  thrpt   10    3423.558 ±    39.249  ops/s
LongLongMapBenchmark.putAll                   SWISS_TABLE_IDENTITY    1000  thrpt   10  467484.448 ±  2167.180  ops/s
LongLongMapBenchmark.putAll                   SWISS_TABLE_IDENTITY  100000  thrpt   10    3421.767 ±   149.698  ops/s
LongLongMapBenchmark.putAll                               HASH_MAP    1000  thrpt   10  138928.555 ±  3809.826  ops/s
LongLongMapBenchmark.putAll                               HASH_MAP  100000  thrpt   10     839.966 ±     9.800  ops/s


Benchmark                                            (mapKind)  (size)   Mode  Cnt      Score      Error  Units
LongLongRangeMapBenchmark.containsExisting      RANGE_TREE_MAP    1000  thrpt   10  79429.545 ±  651.073  ops/s
LongLongRangeMapBenchmark.containsExisting      RANGE_TREE_MAP  100000  thrpt   10    197.016 ±    4.399  ops/s
LongLongRangeMapBenchmark.containsExisting  RANGE_KEY_TREE_MAP    1000  thrpt   10  48644.454 ± 2057.793  ops/s
LongLongRangeMapBenchmark.containsExisting  RANGE_KEY_TREE_MAP  100000  thrpt   10    176.682 ±    5.108  ops/s
LongLongRangeMapBenchmark.getExisting           RANGE_TREE_MAP    1000  thrpt   10  89626.036 ± 1094.686  ops/s
LongLongRangeMapBenchmark.getExisting           RANGE_TREE_MAP  100000  thrpt   10    198.191 ±    0.975  ops/s
LongLongRangeMapBenchmark.getExisting       RANGE_KEY_TREE_MAP    1000  thrpt   10  47828.638 ± 1468.857  ops/s
LongLongRangeMapBenchmark.getExisting       RANGE_KEY_TREE_MAP  100000  thrpt   10    175.858 ±    3.805  ops/s
LongLongRangeMapBenchmark.getMissing            RANGE_TREE_MAP    1000  thrpt   10  83416.467 ± 8685.003  ops/s
LongLongRangeMapBenchmark.getMissing            RANGE_TREE_MAP  100000  thrpt   10    192.624 ±    1.155  ops/s
LongLongRangeMapBenchmark.getMissing        RANGE_KEY_TREE_MAP    1000  thrpt   10  48122.831 ± 3223.855  ops/s
LongLongRangeMapBenchmark.getMissing        RANGE_KEY_TREE_MAP  100000  thrpt   10    175.753 ±    2.325  ops/s
LongLongRangeMapBenchmark.putAll                RANGE_TREE_MAP    1000  thrpt   10  14740.120 ±   52.697  ops/s
LongLongRangeMapBenchmark.putAll                RANGE_TREE_MAP  100000  thrpt   10     69.584 ±    1.810  ops/s
LongLongRangeMapBenchmark.putAll            RANGE_KEY_TREE_MAP    1000  thrpt   10  10032.486 ±   37.437  ops/s
LongLongRangeMapBenchmark.putAll            RANGE_KEY_TREE_MAP  100000  thrpt   10     39.236 ±    0.635  ops/s
LongLongRangeMapBenchmark.putCoalescingAll      RANGE_TREE_MAP    1000  thrpt   10   9687.511 ±  337.462  ops/s
LongLongRangeMapBenchmark.putCoalescingAll      RANGE_TREE_MAP  100000  thrpt   10     43.072 ±    1.630  ops/s
LongLongRangeMapBenchmark.putCoalescingAll  RANGE_KEY_TREE_MAP    1000  thrpt   10   6575.522 ±  203.248  ops/s
LongLongRangeMapBenchmark.putCoalescingAll  RANGE_KEY_TREE_MAP  100000  thrpt   10     24.329 ±    0.366  ops/s
LongLongRangeMapBenchmark.removeAll             RANGE_TREE_MAP    1000  thrpt   10  35105.537 ± 1168.302  ops/s
LongLongRangeMapBenchmark.removeAll             RANGE_TREE_MAP  100000  thrpt   10    142.669 ±    2.225  ops/s
LongLongRangeMapBenchmark.removeAll         RANGE_KEY_TREE_MAP    1000  thrpt   10  25388.732 ±  751.595  ops/s
LongLongRangeMapBenchmark.removeAll         RANGE_KEY_TREE_MAP  100000  thrpt   10    170.237 ±    5.297  ops/s
```

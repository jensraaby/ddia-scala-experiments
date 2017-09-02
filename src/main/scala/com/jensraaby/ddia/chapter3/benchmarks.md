# Benchmarks

To run all the benchmarks, use the SBT command `jmh:run -i 20 -wi 10 -f1 -t1`.

## SimpleLog (append-only store)

The random access benchmark uses a simple KV store with 1000 keys.
100 of the keys are updated with new values to ensure the location of a given key is not always the original location.

The simple log with O(N) search is sped up a lot with a simple HashMap-based index.

```
[info] Benchmark                                               Mode  Cnt     Score     Error   Units
[info] SimpleLogBenchmark.testRandomAccessSimpleLog           thrpt   20    90.582 ±   2.962  ops/ms
[info] SimpleLogBenchmark.testRandomAccessSimpleLogWithIndex  thrpt   20  6574.742 ± 253.090  ops/ms
```
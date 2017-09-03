# Implementations

## SimpleLog
A KV Store with a mutable Buffer as it's implementation.
Each write goes at the end of the fuffer. 
Reads loop through the entire buffer, getting the last value for the given key.

## SimpleLogWithIndex
Extends SimpleLog to store an index as a mutable Hashmap linking keys to offsets in the buffer.
Each put updates the index, so get operations are much faster.

## SimpleLogWithMultipleSegments
This splits the buffer in to multiple segments (each segment is a SimpleLogWithIndex).
Puts are always done in the last segment. When the segment is full, a new one is created.
Gets search backwards from the last segment.

There is a log compaction mechanism which removes outdated values for keys in all but the last segment.


# Benchmarks

To run all the benchmarks, use the SBT command `jmh:run -i 20 -wi 10 -f1 -t1`.
This runs 20 iterations after 10 warmups, with one fork of the JVM and one thread.

## SimpleLog (append-only stores)

The random access benchmark uses a simple KV store with 1000 keys.
100 of the keys are updated with new values to ensure the location of a given key is not always the original location.

The simple log with O(N) search is sped up a lot with a simple HashMap-based index.
Splitting the log into segments of size 100 slows down the random access by about 14%.

```
[info] Benchmark                                                  Mode  Cnt     Score     Error   Units
[info] SimpleLogBenchmark.testRandomAccessSimpleLog              thrpt   20    87.521 ±   3.586  ops/ms
[info] SimpleLogBenchmark.testRandomAccessSimpleLogWithIndex     thrpt   20  7831.511 ± 133.418  ops/ms
[info] SimpleLogBenchmark.testRandomAccessSimpleLogWithSegments  thrpt   20  6745.577 ±  49.031  ops/ms
```

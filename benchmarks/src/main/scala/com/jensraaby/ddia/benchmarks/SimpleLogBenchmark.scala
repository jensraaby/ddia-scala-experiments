package com.jensraaby.ddia.benchmarks

import java.util.concurrent.TimeUnit

import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}
import com.jensraaby.ddia.chapter3.{KVStore, SimpleLog, SimpleLogWithIndex}
import org.openjdk.jmh.annotations._

import scala.util.Random

trait KVStoreHelpers {
  def randomKey: Key = (new Random().nextInt(1000) + 1).toString
  def randomValue: Value = new Random().nextString(5)

  def updateRandomKey(kVStore: KVStore): Unit = {
    kVStore.put(randomKey, randomValue)
  }

  def initialiseData(kVStore: KVStore): Unit = {
    (1 to 1000) foreach { i =>
      kVStore.put(i.toString, randomValue)
    }

    (1 to 100) foreach { _ =>
      updateRandomKey(kVStore)
    }
  }
}

@State(Scope.Benchmark)
class SimpleLogBenchmarkState extends KVStoreHelpers {
  val simple = new SimpleLog

 initialiseData(simple)
}

@State(Scope.Benchmark)
class SimpleLogWithIndexBenchmarkState extends KVStoreHelpers {
  val simpleWithIndex = new SimpleLogWithIndex
  initialiseData(simpleWithIndex)
}


@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class SimpleLogBenchmark {

  @Benchmark
  def testRandomAccessSimpleLog(benchmarkState: SimpleLogBenchmarkState): Unit = {
    benchmarkState.simple.get(benchmarkState.randomKey)
  }

  @Benchmark
  def testRandomAccessSimpleLogWithIndex(benchmarkState: SimpleLogWithIndexBenchmarkState): Unit = {
    benchmarkState.simpleWithIndex.get(benchmarkState.randomKey)
  }
}

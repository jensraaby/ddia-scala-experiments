package com.jensraaby.ddia.chapter3
import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.collection.mutable

/**
  * This basically assumes that there is no concurrency/risk of crashing....
  */
class SimpleLog(initial: Seq[(Key, Value)] = List.empty) extends KVStore {
  protected[this] val log: mutable.Buffer[(Key, Value)] = mutable.Buffer(initial: _*)

  override def get(key: Key): Option[Value] = log.foldLeft(Option.empty[Value]) {
    case (acc, (k, v)) =>
      if (k == key) Some(v) else acc
  }

  override def put(key: Key, value: Value): Unit = log.append((key, value))
}

object SimpleTest extends App {

  val simple = new SimpleLog

  val simpleWithIndex = new SimpleLogWithIndex

  (1 to 1000) foreach { i =>
    simple.put(i.toString, s"value + $i")
    simpleWithIndex.put(i.toString, s"valueWI + $i")
  }

  println(simple.get("6"))
  println(simpleWithIndex.get("6"))

  println(simple.get("1000"))
  println(simpleWithIndex.get("1000"))

  (1 to 1000 by 5) foreach { i =>
    simple.put(i.toString, "newVal")
    simpleWithIndex.put(i.toString, "newVal")
  }

  println(simple.get("6"))
  println(simpleWithIndex.get("6"))

  println(simple.get("1000"))
  println(simpleWithIndex.get("1000"))

}
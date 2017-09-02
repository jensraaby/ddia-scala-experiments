package com.jensraaby.ddia.chapter3
import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.collection.mutable

/**
  * This basically assumes that there is no concurrency/risk of crashing....
  */
class SimpleLog extends KVStore {
  protected[this] val log: mutable.Buffer[(Key, Value)] = mutable.Buffer.empty

  override def get(key: Key): Option[Value] = log.foldLeft(Option.empty[Value]) {
    case (acc, (k, v)) =>
      if (k == key) Some(v) else acc
  }

  override def put(key: Key, value: Value): Unit = log.append((key, value))
}

class SimpleLogWithIndex extends SimpleLog {
  private[this] val index: mutable.Map[Key, Int] = mutable.HashMap.empty

  override def get(key: Key): Option[Value] = {
    // check whether we have the offset, then use it to get the value
    val offset = index.get(key)
    offset.map(idx => log(idx)._2)
  }


  override def put(key: Key, value: Value): Unit = {
    log.append((key, value))
    index.put(key, log.length - 1)
  }
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
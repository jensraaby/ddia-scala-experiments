package com.jensraaby.ddia.chapter3

import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.collection.mutable

class SimpleLogWithIndex(initial: Seq[(Key, Value)] = List.empty) extends SimpleLog(initial) {

  private[this] val index: mutable.Map[Key, Int] = mutable.HashMap.empty

  // initialise the index with the initial values
  log.zipWithIndex.foreach { case ((k, _), i) =>
    index.put(k, i)
  }

  override def get(key: Key): Option[Value] = {
    // check whether we have the offset, then use it to get the value
    val offset = index.get(key)
    offset.map(idx => log(idx)._2)
  }


  override def put(key: Key, value: Value): Unit = {
    log.append((key, value))
    index.put(key, log.length - 1)
  }

  def size: Int = log.size

  def keys: Iterable[Key] = index.keys

  // this is only exposed for KVStores that need to be able to merge/compact logs
  private[chapter3] def segment: mutable.Buffer[(Key, Value)] = log

  override def toString = s"SimpleLogWithIndex($log)"
}

package com.jensraaby.ddia.chapter3
import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.collection.mutable

class SSTable extends KVStore with Compaction {
  private[this] val segments = mutable.Buffer[SortedLogSegment]()

  override def get(key: Key): Option[Value] = ???

  override def put(key: Key, value: Value): Unit = ???


  private def mergeSegments(): Unit = ???
}

class SortedLogSegment extends KVStore {
  private[this] val log: mutable.Buffer[(Key, Value)] = mutable.Buffer.empty
  private[this] val index: mutable.ListMap[Key, Int] = mutable.ListMap.empty

  def sortedKeys: Iterable[Key] = index.keys

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

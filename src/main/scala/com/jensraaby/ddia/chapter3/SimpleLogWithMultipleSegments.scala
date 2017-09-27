package com.jensraaby.ddia.chapter3
import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.annotation.tailrec
import scala.collection.mutable

class SimpleLogWithMultipleSegments(maxSegmentSize: Int = 100) extends KVStore with Compaction {
  private[this] val segments: mutable.Buffer[SimpleLogWithIndex] = mutable.Buffer(new SimpleLogWithIndex)

  override def get(key: Key): Option[Value] = {
    // This is implemented with a recursive lookup from the last segment to the first segment.

    val reversedSegments = segments.reverseIterator

    @tailrec
    def go(iterator: Iterator[SimpleLogWithIndex]): Option[Value] = {
      if (iterator.hasNext) {
        val hasKey = iterator.next().get(key)
        hasKey match {
          case None => go(iterator)
          case Some(value) => Some(value)
        }
      }
      else None
    }

    go(reversedSegments)
  }

  override def put(key: Key, value: Value): Unit = {
    if (currentSegment.size == maxSegmentSize) {
      addSegment()
    }
    currentSegment.put(key, value)
  }

  /**
    * This deduplicates values from all but the current segment
    */
  def performCompaction(): Unit = {
    val segmentsToCompact = segments.slice(0, segments.size - 1)

    val allSegments = segmentsToCompact.map(_.segment)

    val reduced: mutable.Buffer[(Key, Value)] = allSegments.fold(mutable.Buffer.empty)(compactSegments)

    // remove the uncompacted segments
    segments.trimStart(segments.size - 1)

    // add the compacted segment(s)
    val slidingSegments = reduced.grouped(maxSegmentSize).map(segment => new SimpleLogWithIndex(segment))
    segments.prependAll(slidingSegments)
  }

  private[this] def addSegment(): Unit = segments.append(new SimpleLogWithIndex())

  private[this] def currentSegment: SimpleLogWithIndex = segments.last
}


object SSTableTest extends App {

  val ss = new SimpleLogWithMultipleSegments(5)

  ss.put("a", "1")
  ss.put("b", "1")
  ss.put("c", "1")
  ss.put("d", "1")
  ss.put("e", "1")

  println(ss.get("a"))
  println(ss.get("f"))

  ss.put("f", "1")
  ss.put("g", "1")
  ss.put("a", "2")
  ss.put("b", "2")
  ss.put("c", "2")

  println(ss.get("a"))
  println(ss.get("f"))

  ss.put("a", "3")
  ss.put("c", "3")
  ss.put("b", "3")
  ss.put("f", "3")

  println(ss.get("a"))
  println(ss.get("f"))

  ss.performCompaction()

  println(ss.get("a"))
  println(ss.get("f"))

}
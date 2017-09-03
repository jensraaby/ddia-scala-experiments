package com.jensraaby.ddia.chapter3
import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}
import com.jensraaby.ddia.chapter3.SimpleLogWithMultipleSegments.Segment

import scala.annotation.tailrec
import scala.collection.mutable

class SimpleLogWithMultipleSegments(maxSegmentSize: Int = 100) extends KVStore {
  private[this] val segments: mutable.Buffer[SimpleLogWithIndex] = mutable.Buffer(new SimpleLogWithIndex)

  override def get(key: Key): Option[Value] = {
    val seg = segments.reverseIterator

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

    go(seg)
  }


//    segments.foldLeft(Option.empty[Value]) { case (acc, segment) =>
//    // just loop left to right over time (should really go backwards!)
//    // TODO rewrite as recursion from right to left
//    segment.get(key)
//  }

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

    val combinedSegments: mutable.Buffer[(Key, Option[Value])] = segmentsToCompact.flatMap { simpleLog =>
      simpleLog.keys.map(key => (key, simpleLog.get(key)))
    }

    val allCompactedTuples = combinedSegments
      .groupBy(_._1)
      .map { case (key, tuples) =>
        // always use the right hand (i.e. newer) value
        tuples.reduce { (left, right) => (key, right._2) }
      }
      .mapValues(_.get) // should be safe as we only used valid keys to build the structure
      .toBuffer

    // remove the uncompacted segments
    segments.trimStart(segments.size - 1)

    // add the compacted segment(s)
    val slidingSegments = allCompactedTuples.grouped(maxSegmentSize).map(segment => new SimpleLogWithIndex(segment))
    segments.prependAll(slidingSegments)
  }

  private[this] def getFromSegment(segment: Segment, key: Key): Option[Value] =
   segment.foldLeft(Option.empty[Value]) { case (acc, (k, v)) =>
     if (k==key) Some(v)
     else acc
   }

  private[this] def addSegment(): Unit = segments.append(new SimpleLogWithIndex())

  private[this] def currentSegment: SimpleLogWithIndex = segments.last
}

object SimpleLogWithMultipleSegments {
  type Segment = mutable.Buffer[(Key, Value)]
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
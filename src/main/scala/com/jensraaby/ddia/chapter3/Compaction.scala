package com.jensraaby.ddia.chapter3

import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

import scala.collection.mutable

trait Compaction {
  def compactSegments(segment1: mutable.Buffer[(Key, Value)], segment2: mutable.Buffer[(Key, Value)]): mutable.Buffer[(Key, Value)] = {
    val combinedSegments = segment1 ++ segment2

    combinedSegments
      .groupBy(_._1)
      .map { case (key, tuples) =>
        // always use the right hand (i.e. newer) value
        tuples.reduce { (left, right) => (key, right._2) }
      }
      .toBuffer
  }
}

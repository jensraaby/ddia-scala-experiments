package com.jensraaby.ddia.chapter3

import com.jensraaby.ddia.chapter3.KVStore.{Key, Value}

object KVStore {
  // Keep this simple for now...
  type Value = String
  type Key = String
}

trait KVStore {

  def get(key: Key): Option[Value]

  def put(key: Key, value: Value): Unit

}

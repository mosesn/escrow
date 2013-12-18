package com.mosesn.escrow

import scala.collection.concurrent

class ConcurrentMapCache[A, B](underlying: concurrent.Map[A, B]) extends Cache[A, B] {
  def get(a: A): Option[B] = underlying.get(a)

  def set(a: A, b: B) {
    underlying += a -> b
  }

  def evict(a: A, b: B) {
    underlying.remove(a, b)
  }
}

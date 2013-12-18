package com.mosesn.escrow

trait Atomic[A, B] {
  def getOrElseUpdate(a: A, b: () => B): B
}

class SynchronizedCache[A, B](underlying: Cache[A, B]) extends CacheProxy[A, B](underlying) with Atomic[A, B] {
  def getOrElseUpdate(a: A, b: () => B): B = underlying.get(a) getOrElse {
    synchronized {
      underlying.get(a) getOrElse {
        val result = b()
        set(a, result)
        result
      }
    }
  }
}

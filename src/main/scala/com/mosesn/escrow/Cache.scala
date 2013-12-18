package com.mosesn.escrow

import com.twitter.util.Future
import scala.collection.concurrent

/**
 * Each of these actions should be atomic
 */
trait Cache[A, B] extends (A => B) {
  final def apply(a: A): B = get(a).get

  def get(a: A): Option[B]

  def set(a: A, b: B)

  def evict(a: A, b: B)
}

class CacheProxy[A, B](underlying: Cache[A, B]) extends Cache[A, B] {
  def get(a: A): Option[B] = underlying.get(a)

  def set(a: A, b: B) {
    underlying.set(a, b)
  }

  def evict(a: A, b: B) {
    underlying.evict(a, b)
  }
}

object Cache {
  def fromMap[A, B](map: concurrent.Map[A, B]): Cache[A, B] = new ConcurrentMapCache(map)

  def atomic[A, B](cache: Cache[A, B]): Cache[A, B] with Atomic[A, B] = new SynchronizedCache(cache)
}

object Escrow {
  def evicting[A, B](escrow: Escrow[A, B]): EvictingEscrow[A, B] =
    new EvictingEscrow(escrow)
}

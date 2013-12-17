package com.mosesn.escrow

import scala.collection.concurrent

trait Cache[A, B] {
  val fn: A => B
  def get(a: A): B

  def evict(a: A, b: B)
}

class MapCache[A, B](val fn: A => B, map: concurrent.Map[A, B], onWrite: ((Cache[A, B], A, B)) => Unit) extends Cache[A, B] {
  def this(fn: A => B, map: concurrent.Map[A, B]) = this(fn, map, { tup: (Cache[A, B], A, B) => () })

  def get(a: A): B = map.get(a) getOrElse {
    synchronized {
      map.getOrElseUpdate(a, {
        val f = fn(a)
        onWrite(this, a, f)
        f
      })
    }
  }

  def evict(a: A, b: B) {
    map.remove(a, b)
  }
}

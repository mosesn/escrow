package com.mosesn.escrow

class MemoizedFunction[A, B](fn: A => B, atomic: Atomic[A, B]) extends (A => B) {
  def apply(a: A): B = atomic.getOrElseUpdate(a, () => fn(a))
}

object Memo {
  def apply[A, B](fn: A => B, cache: Atomic[A, B]): A => B =
    new MemoizedFunction(fn, cache)

  def atomicized[A, B](fn: A => B, cache: Cache[A, B]): A => B =
    new MemoizedFunction(fn, Cache.atomic(cache))
}

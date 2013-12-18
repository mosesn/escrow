package com.mosesn.escrow

import com.twitter.util.Future

class EvictingEscrow[A, B](underlying: Escrow[A, B]) extends EscrowProxy[A, B](underlying) {
  override def set(a: A, b: RichFuture[B]) {
    b.underlying.onFailure { case t: Throwable =>
      evict(a, b)
    }
  }
}

class EvictingAtomic[A, B](underlying: Escrow[A, B] with Atomic[A, RichFuture[B]]) extends EscrowProxy[A, B](underlying) with Atomic[A, RichFuture[B]]{
  def getOrElseUpdate(a: A, b: () => RichFuture[B]): RichFuture[B] =
    underlying.getOrElseUpdate(a, { () =>
      val result = b()
      result.underlying.onFailure { case t: Throwable =>
        evict(a, result)
      }
      result
    })
}

object Evicting {
  def apply[A, B](underlying: Escrow[A, B]): Escrow[A, B] =
    new EvictingEscrow[A, B](underlying)

  def atomic[A, B](underlying: Escrow[A, B] with Atomic[A, RichFuture[B]]): Escrow[A, B] with Atomic[A, RichFuture[B]] =
    new EvictingAtomic(underlying)
}

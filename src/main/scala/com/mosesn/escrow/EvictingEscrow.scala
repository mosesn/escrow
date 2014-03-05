package com.mosesn.escrow

import com.twitter.util.Future

class EvictingEscrow[A, B](underlying: Escrow[A, B]) extends EscrowProxy[A, B](underlying) {
  override def set(a: A, b: Future[B]) {
    b onFailure { case t: Throwable =>
      evict(a, b)
    }
  }
}

class EvictingAtomic[A, B](underlying: Escrow[A, B] with Atomic[A, Future[B]]) extends EscrowProxy[A, B](underlying) with Atomic[A, Future[B]]{
  def getOrElseUpdate(a: A, b: () => Future[B]): Future[B] =
    underlying.getOrElseUpdate(a, { () =>
      val result = b()
      result onFailure { case t: Throwable =>
        evict(a, result)
      }
      result
    })
}

object Evicting {
  def apply[A, B](underlying: Escrow[A, B]): Escrow[A, B] =
    new EvictingEscrow[A, B](underlying)

  def atomic[A, B](underlying: Escrow[A, B] with Atomic[A, Future[B]]): Escrow[A, B] with Atomic[A, Future[B]] =
    new EvictingAtomic(underlying)
}

package com.mosesn.escrow

import com.twitter.util.Future
import scala.collection.concurrent

object DefaultEscrow {
  def apply[A, B](fn: A => Future[B], map: concurrent.Map[A, Future[B]]): A => Future[B] =
    Interruptible(Memo.atomicized(fn, Escrow.evicting[A, B](Cache.fromMap(map))))
}

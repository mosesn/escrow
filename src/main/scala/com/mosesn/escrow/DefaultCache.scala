package com.mosesn.escrow

import com.twitter.util.Future
import scala.collection.concurrent

object DefaultEscrow {
  def apply[A, B](fn: A => Future[B], map: concurrent.Map[A, RichFuture[B]]): A => Future[B] =
    Interruptible(Memo.atomicized(Enrich(fn), Escrow.evicting[A, B](Cache.fromMap(map))))
}

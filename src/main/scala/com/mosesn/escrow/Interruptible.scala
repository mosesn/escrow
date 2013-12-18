package com.mosesn.escrow

import com.twitter.util.{Future, Memoize, Promise, Time}
import scala.collection.concurrent

object Interruptible {
  def apply[A, B](fn: A => RichFuture[B]): A => Future[B] = { req: A =>
    val f = fn(req)
    val p = Promise[B]()
    p.becomeUnlessInterrupted(f)
    p
  }
}

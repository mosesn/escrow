package com.mosesn.escrow

import com.twitter.util.{Future, Memoize, Promise, Time}
import scala.collection.concurrent

object Interruptible {
  def apply[A, B](fn: A => Future[B]): A => Future[B] = { req: A =>
    val f = fn(req)
    val p = Promise.attached(f)
    p setInterruptHandler { case t: Throwable =>
        if (p.detach())
          p.setException(t)
    }
    p
  }
}

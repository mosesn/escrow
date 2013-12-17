package com.mosesn.escrow

import com.twitter.util.{Closable, Future, Promise}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class RichFuture[Rep](val underlying: Future[Rep]) {
  private[this] val q = new ConcurrentLinkedQueue[Promise[Rep]]
  private[this] val started = new AtomicBoolean()

  def register(p: Promise[Rep]): Closable = synchronized {
    if (underlying.isDefined) {
      p.become(underlying)
      Closable.nop
    } else {
      init()
      q.offer(p)
      Closable.make { _ =>
        synchronized {
          q.remove(p)
        }
        Future.Done
      }
    }
  }

  private[this] def init() {
    if (started.compareAndSet(false, true)) {
      underlying.onSuccess { _ =>
        synchronized {
          q.asScala.foreach { p =>
            p.become(underlying)
          }
        }
        q.clear()
      }
    }
  }
}

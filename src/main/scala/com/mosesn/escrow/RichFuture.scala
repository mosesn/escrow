package com.mosesn.escrow

import com.twitter.util.{Closable, Future, Promise}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class RichFuture[Rep](f: Future[Rep]) {
  private[this] val q = new ConcurrentLinkedQueue[Promise[Rep]]
  private[this] val started = new AtomicBoolean()

  def register(p: Promise[Rep]): Closable = {
    init()
    q.offer(p)
    Closable.make { _ =>
      q.remove(p)
      Future.Done
    }
  }

  private[this] def init() {
    if (started.compareAndSet(false, true)) {
      f.onSuccess { _ =>
        q.asScala.foreach { p =>
          p.become(f)
        }
      }
    }
  }
}

package com.mosesn.escrow

import com.twitter.util.{Closable, Future, Promise, Throw}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantReadWriteLock
import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class RichFuture[A](val underlying: Future[A]) {
  private[this] val lock = new ReentrantReadWriteLock()
  private[this] val reader = lock.readLock()
  private[this] val writer = lock.writeLock()

  private[this] val q = new ConcurrentLinkedQueue[Promise[A]]
  private[this] val started = new AtomicBoolean()

  def register(p: Promise[A]): Closable = if (underlying.isDefined) {
    fulfil(p)
  } else {
    init()
    eventualFulfilment(p)
  }


  private[this] def eventualFulfilment(p: Promise[A]): Closable = {
    reader.lock()
    if (underlying.isDefined) {
      reader.unlock()
      fulfil(p)
    } else {
      q.offer(p)
      reader.unlock()
      mkDetacher(p)
    }
  }

  private[this] def fulfil(p: Promise[A]): Closable = {
    p.become(underlying)
    Closable.nop
  }

  private[this] def mkDetacher(p: Promise[A]): Closable = Closable.make { _ =>
    if (q.remove(p)) {
      p.synchronized {
        p.updateIfEmpty(Throw(new InterruptedException()))
      }
    }
    Future.Done
  }

  private[this] def init() {
    if (started.compareAndSet(false, true)) {
      underlying.respond { _ =>
      }
    }
  }

  private[this] def removeAll() {
    writer.lock()
    q.asScala.foreach { p =>
      p.synchronized {
        if (!p.isDefined) {
          p.become(underlying)
        }
      }
    }
    writer.unlock()
    q.clear()
  }
}

object Enrich {
  def apply[A, B](fn: A => Future[B]): A => RichFuture[B] =
    fn andThen (new RichFuture[B](_))
}

package com.mosesn.escrow.guava

import com.mosesn.escrow.{Cache => MCache, Interruptible, Memo, Enrich, Evicting, RichFuture, Atomic}
import com.google.common.cache.{Cache => GCache}
import com.twitter.util.Future
import java.util.concurrent.Callable

class GuavaCache[A, B](cache: GCache[A, B]) extends MCache[A, B] with Atomic[A, B] {
  def get(a: A): Option[B] = Option(cache.getIfPresent(a))

  def set(a: A, b: B) {
    cache.put(a, b)
  }

  def getOrElseUpdate(a: A, b: () => B): B = cache.get(a, new Callable[B] {
    def call(): B = b()
  })

  def evict(a: A, b: B) {
    cache.invalidate(a)
  }
}

object Guava {
  def cache[A, B](cache: GCache[A, B]): MCache[A, B] with Atomic[A, B] = new GuavaCache(cache)
}

object DefaultGuava {
  def apply[A, B](fn: A => Future[B], cache: GCache[A, RichFuture[B]]): A => Future[B] =
    Interruptible(Memo(Enrich(fn), Evicting.atomic(Guava.cache(cache))))
}

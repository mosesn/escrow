package com.mosesn.escrow

import com.twitter.finagle.{Service, ServiceProxy}
import com.twitter.util.{Future, Memoize, Promise, Time}
import scala.collection.concurrent

/**
 * Generic future cache
 *
 * @cache: lets you pick your memoizing poison
 */
class CacheService[Req, Rep](svc: Service[Req, Rep], cacheMaker: (Req => RichFuture[Rep]) => Cache[Req, RichFuture[Rep]] = CacheService.defaultCache()) extends ServiceProxy[Req, Rep](svc) {
  private[this] val cache = cacheMaker({ req: Req => new RichFuture(svc(req)) })

  override def apply(req: Req): Future[Rep] = {
    val f = cache.get(req)
    val p = Promise[Rep]()
    p.becomeUnlessInterrupted(f)
    p
  }
}


object CacheService {
  def defaultCache[Req, Rep](): (Req => RichFuture[Rep]) => Cache[Req, RichFuture[Rep]] =
    mapToCache(new concurrent.TrieMap())

  def mapToCache[Req, Rep](
    map: concurrent.Map[Req, RichFuture[Rep]]
  ): (Req => RichFuture[Rep]) => Cache[Req, RichFuture[Rep]] = { fn: (Req => RichFuture[Rep]) =>
    val cache: Cache[Req, RichFuture[Rep]] = new MapCache(fn, map, { case (c: Cache[Req, RichFuture[Rep]], req: Req, rf: RichFuture[Rep]) =>
      val f = rf.underlying
      f.onFailure { case t: Throwable =>
        c.evict(req, rf)
      }
    })
    cache
  }
}

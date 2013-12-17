package com.mosesn.escrow

import com.twitter.finagle.{Service, ServiceProxy}
import com.twitter.util.{Future, Memoize, Promise, Time}
import scala.collection.concurrent

/**
 * Generic future cache
 *
 * @memo: lets you pick your memoizing poison
 */
class CacheService[Req, Rep](svc: Service[Req, Rep], memo: Memo[Req, RichFuture[Rep]] = CacheService.defaultMemo()) extends ServiceProxy[Req, Rep](svc) {
  private[this] val fn = memo({ req: Req => new RichFuture(svc(req)) })

  override def apply(req: Req): Future[Rep] = {
    val f = fn(req)
    val p = Promise[Rep]()
    p.becomeUnlessInterrupted(f)
    p
  }
}


object CacheService {
  def defaultMemo[Req, Rep](): Memo[Req, RichFuture[Rep]] =
    Memoize.apply[Req, RichFuture[Rep]]

  def mapToMemo[Req, Rep](
    map: concurrent.Map[Req, RichFuture[Rep]]
  ) = { fn: (Req => RichFuture[Rep]) =>
    { req: Req =>
      map.get(req) getOrElse {
        synchronized {
          map.getOrElseUpdate(req, fn(req))
        }
      }
    }
  }
}

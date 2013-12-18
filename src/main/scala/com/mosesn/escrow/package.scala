package com.mosesn

import com.twitter.util.Promise

package object escrow {
  type Escrow[A, B] = Cache[A, RichFuture[B]]
  type EscrowProxy[A, B] = CacheProxy[A, RichFuture[B]]

  implicit def promiseToRichPromise[A](p: Promise[A]): RichPromise[A] =
    new RichPromise(p)
}

package com.mosesn

import com.twitter.util.{Future, Promise}

package object escrow {
  type Escrow[A, B] = Cache[A, Future[B]]
  type EscrowProxy[A, B] = CacheProxy[A, Future[B]]
}

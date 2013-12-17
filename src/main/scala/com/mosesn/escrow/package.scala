package com.mosesn

import com.twitter.util.Promise
package object escrow {
  type Memo[A, B] = (A => B) => (A => B)

  implicit def promiseToRichPromise[A](p: Promise[A]): RichPromise[A] =
    new RichPromise(p)
}

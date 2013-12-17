package com.mosesn.escrow

import com.twitter.util.Promise

class RichPromise[Rep](p: Promise[Rep]) {
  def becomeUnlessInterrupted(f: RichFuture[Rep]) {
    val c = f.register(p)
    p.setInterruptHandler { case _: Throwable =>
      c.close()
    }
  }
}

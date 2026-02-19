package com.kiwi.iointerview.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalamock.stubs.CatsEffectStubs
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class UserRoutesTest extends AsyncFreeSpec with AsyncIOSpec with Matchers with CatsEffectStubs {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

/*  "it should return user" in {
    val 
  }  */
}

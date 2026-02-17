package com.kiwi.iointerview.sse

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.Stream
import org.http4s.*
import org.http4s.ServerSentEvent.EventId
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class SseExampleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private def finiteEvents(n: Int): Stream[IO, ServerSentEvent] =
    Stream
      .range(0, n)
      .covary[IO]
      .map { i =>
        ServerSentEvent(
          data = Some(s"""{"tick": $i}"""),
          eventType = Some("tick"),
          id = Some(EventId(i.toString))
        )
      }

  "SseExample should stream tick events" in {
    val uri = uri"/events"
    val request = Request[IO](Method.GET, uri)

    val client: Client[IO] =
      Client.fromHttpApp(SseExample.routes(finiteEvents(5)).orNotFound)

    val result =
      client
        .stream(request)
        .flatMap(_.body)
        .through(ServerSentEvent.decoder[IO])
        .take(3)
        .compile
        .toList

    result.asserting(_.size shouldBe 3)
  }

  "Real SseExample should stream tick events" in {
    val uri = uri"/events"
    val request = Request[IO](Method.GET, uri)
    val client: Client[IO] =
      Client.fromHttpApp(SseExample.routes(finiteEvents(3)).orNotFound)

    val result =
      client
        .stream(request)
        .flatMap(_.body)
        .through(ServerSentEvent.decoder[IO])
        .compile
        .toList

    result.asserting { r =>
      r.size shouldBe 4 // one empty in bonus
    }
  }
}

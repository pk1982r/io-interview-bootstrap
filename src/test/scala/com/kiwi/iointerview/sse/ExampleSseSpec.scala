package com.kiwi.iointerview.sse

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.Stream
import org.http4s.*
import org.http4s.ServerSentEvent.EventId
import org.http4s.circe.*
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory

// TODO use CE Test time
class SseExampleSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  given LoggerFactory[IO] = LoggerFactory[IO]

  given EntityDecoder[IO, List[String]] = jsonOf[IO, List[String]]

  "SseExample should stream tick events" in {

    val testStream =
      Stream
        .range(0, 3)
        .covary[IO]
        .map { i =>
          ServerSentEvent(
            data = Some(s"""{"tick": $i}"""),
            eventType = Some("tick"),
            id = Some(EventId(i.toString))
          )
        }

    val httpApp = HttpRoutes
      .of[IO] { case GET -> Root / "events" =>
        Ok(testStream)
      }
      .orNotFound

    val uri = uri"/events" // placeholder port if needed
    val request = Request[IO](Method.GET, uri)
    val client: Client[IO] = Client.fromHttpApp(httpApp)

    val result =
      client
        .stream(request)
        .flatMap(_.body)
        .through(ServerSentEvent.decoder[IO])
        .take(3)
        .compile
        .toList

    result.asserting(_.nonEmpty shouldBe true)
  }
}

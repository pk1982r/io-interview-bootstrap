package com.kiwi.iointerview.sse

import cats.effect.*
import com.comcast.ip4s.Port
import fs2.Stream
import org.http4s.*
import org.http4s.ServerSentEvent.EventId
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory

import scala.concurrent.duration.*

object SseExample extends IOApp {

  def events(period: FiniteDuration): Stream[IO, ServerSentEvent] =
    Stream
      .awakeEvery[IO](period)
      .zipWithIndex
      .map { case (_, i) =>
        ServerSentEvent(
          data = Some(s"""{"tick": $i}"""),
          eventType = Some("tick"),
          id = Some(EventId(i.toString))
        )
      }

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "events" =>
      Ok(events(1.second))
  }

  given LoggerFactory[IO] = LoggerFactory[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    IO.println("Starting server on port 8080") *>
      EmberServerBuilder
        .default[IO]
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(routes.orNotFound)
        .build
        .useForever
        .as(ExitCode.Success)
  }
}

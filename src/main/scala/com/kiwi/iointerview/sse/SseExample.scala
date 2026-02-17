package com.kiwi.iointerview.sse

import cats.effect.*
import com.comcast.ip4s.Port
import fs2.Stream
import org.http4s.*
import org.http4s.ServerSentEvent.EventId
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.duration.*

object SseExample extends IOApp {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  def events(period: FiniteDuration): Stream[IO, ServerSentEvent] = {
    val logger = summon[LoggerFactory[IO]].getLogger

    Stream
      .awakeEvery[IO](period)
      .evalTap(_ => logger.info("tick"))
      .zipWithIndex
      .map { case (_, i) =>
        ServerSentEvent(
          data = Some(s"""{"tick": $i}"""),
          eventType = Some("tick"),
          id = Some(EventId(i.toString))
        )
      }
  }

  def routes(eventStream: Stream[IO, ServerSentEvent]): HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "events" => Ok(eventStream) }

  val defaultRoutes: HttpRoutes[IO] = routes(events(1.second))

  override def run(args: List[String]): IO[ExitCode] = {
    IO.println("Starting server on port 8080") *>
      EmberServerBuilder
        .default[IO]
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(defaultRoutes.orNotFound)
        .build
        .useForever
        .as(ExitCode.Success)
  }
}

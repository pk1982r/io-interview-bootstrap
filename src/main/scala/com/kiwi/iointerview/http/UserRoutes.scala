package com.kiwi.iointerview.http
import cats.*
import cats.data.{Kleisli, OptionT}
import cats.effect.*
import cats.implicits.catsSyntaxEq
import cats.syntax.all.*
import com.kiwi.iointerview.db.UserRepository
import com.kiwi.iointerview.model.User
import com.kiwi.iointerview.model.User.given
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.server.AuthMiddleware
import org.typelevel.ci.CIString
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.duration.DurationInt

class UserRoutes(userRepository: UserRepository) {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  val logger = summon[LoggerFactory[IO]].getLogger

  // TODO validation and maybe Tapir required - will be introduced later
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "user" / LongVar(id) =>
      userRepository
        .findById(id)
        .timeout(5.seconds)
        .flatMap {
          case Some(user) => Ok(user)
          case None       => NotFound()
        }
        .handleErrorWith { error =>
          logger.error(s"Error: $error") *>
            InternalServerError("Please try later")
        }
    }

  def authRoutes: AuthedRoutes[Unit, IO] =
    AuthedRoutes.of { case req @ PUT -> Root / "user" / LongVar(id) as _ =>
      (for {
        user <- req.req.as[User]
        out <- userRepository.insert(user).timeout(5.seconds)
        resp <- Ok(out)
      } yield resp).handleErrorWith { error =>
        logger.error(s"Error: $error") *>
          InternalServerError("Please try later")
      }
    }

  // https://http4s.org/v1/docs/auth.html
  private val adminToken = "kiwi_admin@kiwi.com"

  val authUser: Kleisli[OptionT[IO, *], Request[IO], Unit] =
    Kleisli { rq =>
      OptionT.fromOption[IO] {
        rq.headers
          .get(CIString("token"))
          .filter(_.head.value === adminToken)
          .map(_ => ())
      }
    }

  private val middleware: AuthMiddleware[IO, Unit] =
    AuthMiddleware(authUser)

  val service: HttpRoutes[IO] =
    middleware(authRoutes) <+> routes

}

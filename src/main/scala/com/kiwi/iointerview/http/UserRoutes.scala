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

class UserRoutes(userRepository: UserRepository) {

  // TODO validation and maybe Tapir required - will be introduced later
  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "user" / LongVar(id) =>
      userRepository.findById(id).flatMap {
        case Some(user) => Ok(user)
        case None       => NotFound()
      }
    }

  def authRoutes: AuthedRoutes[Either[String, User], IO] =
    AuthedRoutes.of { case PUT -> Root / "user" / LongVar(id) as authed =>
      authed match {
        case Right(user) =>
          for {
            out <- userRepository.insert(user)
            resp <- Ok(out)
          } yield resp
        case Left(err) => BadRequest(err)
      }
    }

  // https://http4s.org/v1/docs/auth.html
  private val adminToken = "kiwi_admin@kiwi.com"

  val authUser: Kleisli[OptionT[IO, *], Request[IO], Either[String, User]] =
    Kleisli { rq =>
      val out: IO[Either[String, User]] = for {
        token <- IO(
          rq.headers.get(CIString("token")).toRight("Missing token header")
        )
        user <- rq.as[User]
        authResult = token.flatMap { token =>
          if token.head.value === adminToken then Right(user)
          else Left("Invalid token")
        }
      } yield authResult
      OptionT.liftF(out)
    }

  val middleware: AuthMiddleware[IO, Either[String, User]] =
    AuthMiddleware(authUser)

  val service: HttpRoutes[IO] =
    middleware(authRoutes) <+> routes

}

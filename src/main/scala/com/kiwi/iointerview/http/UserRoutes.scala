package com.kiwi.iointerview.http
import cats.*
import cats.effect.*
import com.kiwi.iointerview.db.UserRepository
import com.kiwi.iointerview.model.User.given
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*

class UserRoutes(userRepository: UserRepository) {

  def routes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root / "user" / LongVar(id) =>
      userRepository.findById(id).flatMap {
        case Some(user) => Ok(user)
        case None       => NotFound()
      }
    }
}

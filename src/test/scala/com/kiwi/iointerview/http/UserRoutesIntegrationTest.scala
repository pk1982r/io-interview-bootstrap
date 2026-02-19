package com.kiwi.iointerview.http

import cats.effect.*
import com.kiwi.iointerview
import com.kiwi.iointerview.db.UserRepositoryImpl
import com.kiwi.iointerview.integration.PgIntegrationTest
import com.kiwi.iointerview.model.User
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class UserRoutesIntegrationTest extends PgIntegrationTest {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  "it should return a user" in withTransactor { xa =>
    val userRepo = UserRepositoryImpl(xa)
    val userRoutes = new UserRoutes(userRepo)
    val req = Request[IO](Method.GET, uri"/user/1")
    for {
      _ <- userRepo.insert(iointerview.user)
      response <- userRoutes.routes.orNotFound(req)
      userReturned <- response.as[User]
    } yield {
      val _ = response.status.code shouldBe 200
      userReturned shouldBe iointerview.user
    }
  }
}

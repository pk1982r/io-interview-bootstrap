package com.kiwi.iointerview.http

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxOptionId
import com.kiwi.iointerview
import com.kiwi.iointerview.db.UserRepository
import com.kiwi.iointerview.model.User
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.scalamock.stubs.{CatsEffectStubs, Stub}
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

// TODO integration test will be added
class UserRoutesTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CatsEffectStubs {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  "it should return a user" in withStub { stubUserRepository =>
    val userRoutes = new UserRoutes(stubUserRepository)
    val req = Request[IO](Method.GET, uri"/user/1")
    for {
      _ <- stubUserRepository.findById.succeedsWith(iointerview.user.some)
      response <- userRoutes.routes.orNotFound(req)
      userReturned <- response.as[User]
    } yield {
      val _ = response.status.code shouldBe 200
      userReturned shouldBe iointerview.user
    }
  }

  "it should Not return a user" in withStub { stubUserRepository =>
    val userRoutes = new UserRoutes(stubUserRepository)
    val req = Request[IO](Method.GET, uri"/user/1")
    for {
      _ <- stubUserRepository.findById.succeedsWith(None)
      response <- userRoutes.routes.orNotFound(req)
    } yield response.status.code shouldBe 404
  }

  def withStub(test: Stub[UserRepository] => IO[Assertion]): IO[Assertion] = {
    val stubUserRepository = stub[UserRepository]
    test(stubUserRepository)
  }
}

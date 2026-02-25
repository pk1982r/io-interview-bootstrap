package com.kiwi.iointerview.http

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.kiwi.iointerview
import com.kiwi.iointerview.db.UserRepository
import com.kiwi.iointerview.model.User
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import org.scalamock.stubs.{CatsEffectStubs, Stub}
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

class UserAuthRoutesTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with CatsEffectStubs {
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  "it should return 401 when token is missing" in withStub { (_, userRoutes) =>
    val req = Request[IO](Method.PUT, uri"/user/1")
      .withEntity(iointerview.user)

    for {
      response <- userRoutes.service.orNotFound(req)
    } yield {
      response.status shouldBe Status.Unauthorized
    }
  }

  "it should return a user" in withStub { (stubUserRepository, userRoutes) =>
    val req = Request[IO](Method.PUT, uri"/user/1")
      .withEntity(iointerview.user)
      .withContentType(`Content-Type`(MediaType.application.json))
      .putHeaders(Header.Raw(CIString("token"), "kiwi_admin@kiwi.com"))

    for {
      _ <- stubUserRepository.insert.succeedsWith(111L)
      response <- userRoutes.service.orNotFound(req)
      idReturned <- response.as[Long]
    } yield {
      val _ = response.status.code shouldBe 200
      idReturned shouldBe 111L
    }
  }

  def withStub(
      test: ((Stub[UserRepository], UserRoutes)) => IO[Assertion]
  ): IO[Assertion] = {
    val stubUserRepository = stub[UserRepository]
    val userRoutes = new UserRoutes(stubUserRepository)
    test((stubUserRepository, userRoutes))
  }
}

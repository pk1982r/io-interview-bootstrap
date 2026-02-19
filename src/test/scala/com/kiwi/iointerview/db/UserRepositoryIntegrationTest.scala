package com.kiwi.iointerview.db

import com.kiwi.iointerview.integration.{
  TestUserRepository,
  TwoPgIntegrationTest
}
import com.kiwi.iointerview.model.User
import com.kiwi.iointerview.userFromId
import doobie.implicits.toConnectionIOOps

import java.time.Instant

class UserRepositoryIntegrationTest extends TwoPgIntegrationTest {

  "it should have two separate DBs" in withTransactors { (xa, xb) =>
    val userRepositoryA = new UserRepositoryImpl(xa)
    val userRepositoryB = new UserRepositoryImpl(xb)

    val emailAUser = User("11L", "testA@test.com", Instant.now())
    val emailBUser = User("22L", "testB@test.com", Instant.now())
    for {
      _ <- userRepositoryA.insert(emailAUser)
      _ <- userRepositoryB.insert(emailBUser)

      userAinA <- userRepositoryA.findByEmail(emailAUser.email)
      userBinA <- userRepositoryA.findByEmail(emailBUser.email)

      userAinB <- userRepositoryB.findByEmail(emailAUser.email)
      userBinB <- userRepositoryB.findByEmail(emailBUser.email)
    } yield {
      val _ = userAinA.isDefined shouldBe true
      val _ = userAinA.get.email shouldBe emailAUser.email

      val _ = userBinA.isDefined shouldBe false

      val _ = userAinB.isDefined shouldBe false

      val _ = userBinB.isDefined shouldBe true
      userBinB.get.email shouldBe emailBUser.email
    }
  }

  "it should batch insert using the script" in withTransactors { (xa, xb) =>
    val numberOfUsers = 1000
    val userRepository = new UserRepositoryImpl(xa)

    for {
      _ <- TestUserRepository.truncate.transact(xa)
      users = List.tabulate(numberOfUsers)(_.userFromId)
      _ <- userRepository.insertBatch_(users)
      numberOfUsersA <- TestUserRepository.count.transact(xa)
    } yield {
      numberOfUsersA shouldBe numberOfUsers
    }
  }
}

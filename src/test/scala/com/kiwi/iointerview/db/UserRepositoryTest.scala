package com.kiwi.iointerview.db

import com.kiwi.iointerview.integration.{TestUserRepository, TwoPgIntegrationTest}
import com.kiwi.iointerview.model.User
import com.kiwi.iointerview.userFromId
import doobie.implicits.toConnectionIOOps

import java.time.Instant

class UserRepositoryTest extends TwoPgIntegrationTest {

  import UserRepository.*

  "it should have two separate DBs" in withTransactors { (xa, xb) =>
    val emailAUser = User("11L", "testA@test.com", Instant.now())
    val emailBUser = User("22L", "testB@test.com", Instant.now())
    for {
      _ <- insert(emailAUser).transact(xa)
      _ <- insert(emailBUser).transact(xb)

      userAinA <- findByEmail(emailAUser.email).transact(xa)
      userBinA <- findByEmail(emailBUser.email).transact(xa)

      userAinB <- findByEmail(emailAUser.email).transact(xb)
      userBinB <- findByEmail(emailBUser.email).transact(xb)
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
    for {
      _ <- TestUserRepository.truncate.transact(xa)
      users = List.tabulate(numberOfUsers)(_.userFromId)
      _ <- insertBatch_(users).transact(xa)
      numberOfUsersA <- TestUserRepository.count.transact(xa)
    } yield {
      numberOfUsersA shouldBe numberOfUsers
    }
  }
}

package com.kiwi

import com.kiwi.iointerview.model.User

import java.time.Instant

package object iointerview {
  val user: User = User(
    externalId = "external-001",
    email = "test001@gmail.com",
    createdAt = Instant.parse("2023-01-01T00:00:00Z")
  )

  extension (i: Int) {
    def userFromId: User = {
      User(
        s"$i",
        s"test$i@dot.com",
        Instant.now()
      ) // TODO replace with CE TestControl when required
    }
  }
}

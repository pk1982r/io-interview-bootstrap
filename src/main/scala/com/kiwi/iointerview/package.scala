package com.kiwi

import com.kiwi.iointerview.model.User

import java.time.Instant

package object iointerview {
  extension (i: Int) {
    def userFromId: User = {
      User(s"$i", s"test$i@dot.com", Instant.now()) // TODO replace with CE TestControl when required
    }
  }
}

package com.kiwi.iointerview.model

import java.time.Instant

final case class User(
                       externalId: String,
                       email: String,
                       createdAt: Instant
                     )

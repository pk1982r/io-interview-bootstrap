package com.kiwi.iointerview.model
import io.circe.Codec
import io.circe.generic.semiauto.*

import java.time.Instant

final case class User(
    externalId: String,
    email: String,
    createdAt: Instant
) derives Codec.AsObject

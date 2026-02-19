package com.kiwi.iointerview.db

import cats.effect.IO
import cats.implicits.*
import com.kiwi.iointerview.model.User
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*

trait UserRepository {
  def insert(user: User): IO[Long]
  def insertBatch(users: List[User]): IO[List[Long]]
  def insertBatch_(users: List[User]): IO[Int]
  def findById(id: Long): IO[Option[User]]
  def findByExternalId(externalId: String): IO[Option[User]]
  def findByEmail(email: String): IO[Option[User]]
  def findAll: IO[List[User]]
}
//noinspection SqlNoDataSourceInspection
class UserRepositoryImpl(xa: Transactor[IO]) extends UserRepository {

  def insert(user: User): IO[Long] =
    sql"""
      INSERT INTO users (external_id, email, created_at)
      VALUES (${user.externalId}, ${user.email}, ${user.createdAt})
      RETURNING id
    """
      .query[Long]
      .unique
      .transact(xa)

  private val insertUserUpdate: Update[User] =
    Update[User](
      """
        INSERT INTO users (external_id, email, created_at)
        VALUES (?, ?, ?)
      """
    )

  def insertBatch(users: List[User]): IO[List[Long]] =
    insertUserUpdate
      .updateManyWithGeneratedKeys[Long]("id")(users)
      .compile
      .toList
      .transact(xa)

  def insertBatch_(users: List[User]): IO[Int] =
    insertUserUpdate
      .updateMany(users)
      .transact(xa)

  def findById(id: Long): IO[Option[User]] =
    sql"""
      SELECT external_id, email, created_at
      FROM users
      WHERE id = $id
    """
      .query[User]
      .option
      .transact(xa)

  def findByExternalId(externalId: String): IO[Option[User]] =
    sql"""
      SELECT external_id, email, created_at
      FROM users
      WHERE external_id = $externalId
    """
      .query[User]
      .option
      .transact(xa)

  def findByEmail(email: String): IO[Option[User]] =
    sql"""
      SELECT external_id, email, created_at
      FROM users
      WHERE email = $email
    """
      .query[User]
      .option
      .transact(xa)

  def findAll: IO[List[User]] =
    sql"""
      SELECT external_id, email, created_at
      FROM users
      ORDER BY id
    """
      .query[User]
      .to[List]
      .transact(xa)
}

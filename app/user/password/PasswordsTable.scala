package user.password

import domain.Password
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait PasswordsTable
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  class Passwords(tag: Tag) extends Table[Password](tag, "passwords") {
    def userEmail = column[String]("userEmail", O.PrimaryKey)
    def hasher = column[String]("hasher")
    def hash = column[String]("hash")
    def salt = column[Option[String]]("salt")

    def * = {
      (userEmail, hasher, hash, salt).<>(Password.tupled, Password.unapply)
    }
  }

  val passwords = TableQuery[Passwords]
}

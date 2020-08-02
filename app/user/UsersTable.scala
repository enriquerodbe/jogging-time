package user

import domain.User
import util.filter.{Field, FilterTable}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

private[user] trait UsersTable extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") with FilterTable {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def email = column[String]("email")

    override def * = {
      (id, firstName, lastName, email).<>((User.apply _).tupled, User.unapply)
    }

    override def getColumn[T](field: Field[T]): Rep[T] = {
      field match {
        case f: UserField[_] => f match {
          case UserField.FirstName => firstName.asInstanceOf[Rep[T]]
          case UserField.LastName => lastName.asInstanceOf[Rep[T]]
          case UserField.Email => email.asInstanceOf[Rep[T]]
        }
      }
    }
  }

  val users = TableQuery[Users]
  val usersInsert =
    users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))
}

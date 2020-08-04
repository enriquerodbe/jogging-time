package user

import domain.{User, UserRole}
import domain.UserRole.UserRole
import filter.{Field, FilterTable}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

private[user] trait UsersTable extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val rolesMapper =
    MappedColumnType.base[Set[UserRole], String](
      _.mkString(","),
      _.split(",").filterNot(_.isEmpty).map(UserRole.withName).toSet
    )

  class Users(tag: Tag) extends Table[User](tag, "users") with FilterTable {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def roles = column[Set[UserRole]]("roles")

    override def * = {
      (id, email, firstName, lastName, roles)
        .<>((User.apply _).tupled, User.unapply)
    }

    override def getColumn[T](field: Field[T]): Rep[T] = {
      field match {
        case f: UserField[_] => (f match {
          case UserField.FirstName => firstName
          case UserField.LastName => lastName
          case UserField.Email => email
        }).asInstanceOf[Rep[T]]
      }
    }
  }

  val users = TableQuery[Users]
  val usersInsert =
    users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))
}

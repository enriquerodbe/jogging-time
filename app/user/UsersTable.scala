package user

import domain.{User, UserRole}
import domain.UserRole.UserRole
import filter.database.{FilterDao, FilterTable, FilterColumn}

trait UsersTable extends FilterDao[UserField] {

  import profile.api._

  implicit val rolesMapper: BaseColumnType[Set[UserRole]] =
    MappedColumnType.base[Set[UserRole], String](
      _.mkString(","),
      _.split(",").filterNot(_.isEmpty).map(UserRole.withName).toSet,
    )

  class Users(tag: Tag) extends Table[User](tag, "users") with FilterTable[UserField] {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def firstName = column[String]("firstName")
    def lastName = column[String]("lastName")
    def roles = column[Set[UserRole]]("roles")

    override def * = {
      (id, email, firstName, lastName, roles)
        .<>((User.apply _).tupled, User.unapply)
    }

    override def getFilterColumn[T](field: UserField[T]): FilterColumn[T] =
      field match {
        case UserField.FirstName => firstName
        case UserField.LastName => lastName
        case UserField.Email => email
      }

  }

  val users = TableQuery[Users]

  val usersInsert =
    users.returning(users.map(_.id)).into((user, id) => user.copy(id = id))

}

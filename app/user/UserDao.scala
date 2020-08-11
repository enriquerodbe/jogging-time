package user

import domain.User
import domain.UserRole.UserRole
import filter._
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider

@Singleton
private[user] class UserDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider) extends UsersTable {

  import profile.api._

  def create(user: User): DBIO[User] = usersInsert += user

  def retrieve(id: Long): DBIO[User] = users.filter(_.id === id).result.head

  def retrieve(filter: FilterOptions): DBIO[Seq[User]] = {
    users
      .filter(buildCondition(filter.condition, _))
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  def count(filter: FilterExpression): DBIO[Int] = {
    users.filter(buildCondition(filter, _)).length.result
  }

  def update(user: User): DBIO[Int] = {
    users
      .filter(_.id === user.id)
      .map(u => (u.firstName, u.lastName))
      .update((user.firstName, user.lastName))
  }

  def updateRoles(id: Long, roles: Set[UserRole]): DBIO[Int] = {
    users.filter(_.id === id).map(_.roles).update(roles)
  }

  def delete(id: Long): DBIO[Int] = users.filter(_.id === id).delete
}

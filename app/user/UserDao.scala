package user

import domain.User
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import util.filter.{FilterDao, _}

private[user] class UserDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)
  extends UsersTable with FilterDao {

  import profile.api._

  def create(user: User): DBIO[User] = usersInsert += user

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
    users.filter(_.id === user.id).update(user)
  }

  def delete(id: Long): DBIO[Int] = users.filter(_.id === id).delete
}

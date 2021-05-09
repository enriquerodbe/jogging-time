package user

import com.google.inject.ImplementedBy
import com.mohiva.play.silhouette.api.services.IdentityService
import domain.UserRole.UserRole
import domain.{Page, User}
import filter.FilterOptions
import scala.concurrent.Future

@ImplementedBy(classOf[UserServiceImpl])
trait UserService extends IdentityService[User] {

  def create(user: User, plainPassword: String): Future[User]

  def retrieve(filter: FilterOptions[UserField]): Future[Page[User]]

  def update(user: User): Future[Unit]

  def updateRoles(
      userId: Long,
      add: Set[UserRole],
      remove: Set[UserRole]): Future[Unit]

  def updatePassword(id: Long, password: String): Future[Unit]

  def delete(id: Long): Future[Unit]
}

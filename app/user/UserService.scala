package user

import com.google.inject.ImplementedBy
import com.mohiva.play.silhouette.api.services.IdentityService
import domain.{Page, User}
import scala.concurrent.Future
import util.filter.FilterOptions

@ImplementedBy(classOf[UserServiceImpl])
trait UserService extends IdentityService[User] {

  def create(user: User, plainPassword: String): Future[User]

  def retrieve(filter: FilterOptions): Future[Page[User]]

  def update(user: User): Future[Unit]

  def delete(id: Long): Future[Unit]
}

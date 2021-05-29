package auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator
import domain.User
import domain.UserRole.UserRole
import play.api.mvc.Request
import scala.concurrent.Future

case class Is(role: UserRole) extends Authorization[User, DummyAuthenticator] {

  override def isAuthorized[T](identity: User, authenticator: DummyAuthenticator)(
      implicit request: Request[T]
  ): Future[Boolean] =
    Future.successful(identity.roles.contains(role))

}

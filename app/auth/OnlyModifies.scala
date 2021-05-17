package auth

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator
import domain.User
import domain.UserRole.UserRole
import play.api.mvc.Request
import scala.concurrent.Future
import user.UserRoleDto

case class OnlyModifies(role: UserRole) extends Authorization[User, DummyAuthenticator] {

  override def isAuthorized[B](identity: User, authenticator: DummyAuthenticator)(
      implicit request: Request[B]
  ): Future[Boolean] = {
    val result = request.body match {
      case UserRoleDto(add, remove) => (add ++ remove).forall(_ == role)
      case _ => false
    }
    Future.successful(result)
  }

}

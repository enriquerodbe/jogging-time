package domain

import com.mohiva.play.silhouette.api.Identity
import domain.UserRole.UserRole

case class User(
    id: Long,
    email: String,
    firstName: String,
    lastName: String,
    roles: Set[UserRole],
) extends Identity {

  def is(role: UserRole): Boolean = roles.contains(role)
}

package domain

import com.mohiva.play.silhouette.api.Identity
import domain.UserRole.UserRole
import filter.StringField

case class User(
    id: Long,
    email: String,
    firstName: String,
    lastName: String,
    roles: Set[UserRole],
) extends Identity {

  def is(role: UserRole): Boolean = roles.contains(role)
}

sealed trait UserField extends StringField

object UserField {
  case object Email extends UserField
  case object FirstName extends UserField
  case object LastName extends UserField
}

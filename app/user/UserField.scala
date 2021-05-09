package user

import filter.Field

sealed trait UserField[T] extends Field[T]

object UserField {
  case object Email extends UserField[String]
  case object FirstName extends UserField[String]
  case object LastName extends UserField[String]
}

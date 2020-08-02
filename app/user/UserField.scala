package user

import util.filter.Field

private[user] sealed trait UserField[T] extends Field[T]

private[user] object UserField {

  case object FirstName extends UserField[String]
  case object LastName extends UserField[String]
  case object Email extends UserField[String]
}

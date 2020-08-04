package user

import domain.User

case class UserDto(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
) {
  def user: User = User(-1, email, firstName, lastName, Set.empty)
}

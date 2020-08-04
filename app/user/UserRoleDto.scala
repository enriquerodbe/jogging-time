package user

import domain.UserRole.UserRole

case class UserRoleDto(add: Set[UserRole], remove: Set[UserRole])

package domain

object UserRole extends Enumeration {
  type UserRole = Value

  val Manager = Value("MANAGER")
  val Admin = Value("ADMIN")
}

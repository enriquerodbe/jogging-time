package user.password

private[password] case class Password(
    userEmail: String,
    hasher: String,
    hash: String,
    salt: Option[String],
)

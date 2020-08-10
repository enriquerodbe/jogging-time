package domain

case class Password(
    userEmail: String,
    hasher: String,
    hash: String,
    salt: Option[String],
)

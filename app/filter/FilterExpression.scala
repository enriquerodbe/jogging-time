package filter

sealed trait FilterExpression[F[_] <: Field[_]]

object FilterExpression {
  case class Empty[F[_] <: Field[_]]() extends FilterExpression[F]

  case class Eq[T, F[_] <: Field[_]](field: F[T], value: T) extends FilterExpression[F]
  case class Ne[T, F[_] <: Field[_]](field: F[T], value: T) extends FilterExpression[F]
  case class Gt[T, F[_] <: Field[_]](field: F[T], value: T) extends FilterExpression[F]
  case class Lt[T, F[_] <: Field[_]](field: F[T], value: T) extends FilterExpression[F]

  case class And[F[_] <: Field[_]](
      expr1: FilterExpression[F],
      expr2: FilterExpression[F]) extends FilterExpression[F]

  case class Or[F[_] <: Field[_]](
      expr1: FilterExpression[F],
      expr2: FilterExpression[F]) extends FilterExpression[F]
}

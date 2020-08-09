package filter

sealed trait FilterExpression {

  def and(other: FilterExpression): FilterExpression = And(this, other)
  def or(other: FilterExpression): FilterExpression = Or(this, other)
  def isEmpty: Boolean = false
}

case class Eq[T](field: Field[T], value: T) extends FilterExpression
case class Ne[T](field: Field[T], value: T) extends FilterExpression
case class Gt[T](field: Field[T], value: T) extends FilterExpression
case class Lt[T](field: Field[T], value: T) extends FilterExpression

case class And(
    expr1: FilterExpression,
    expr2: FilterExpression) extends FilterExpression

case class Or(
    expr1: FilterExpression,
    expr2: FilterExpression) extends FilterExpression

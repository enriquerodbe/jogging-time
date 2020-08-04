package filter

sealed trait FilterExpression {

  def and(other: FilterExpression): FilterExpression = And(this, other)
  def or(other: FilterExpression): FilterExpression = Or(this, other)
  def isEmpty: Boolean = false
}

case object EmptyExpression extends FilterExpression {

  override def and(other: FilterExpression): FilterExpression = other
  override def or(other: FilterExpression): FilterExpression = other
  override def isEmpty: Boolean = true
}

case class StringEq(field: Field[String], value: String) extends FilterExpression
case class StringNe(field: Field[String], value: String) extends FilterExpression

case class And(
    exp1: FilterExpression, exp2: FilterExpression) extends FilterExpression
case class Or(
    exp1: FilterExpression, exp2: FilterExpression) extends FilterExpression

object FilterExpression {

  def stringEq(field: Field[String], value: Option[String]): FilterExpression = {
    value.map(StringEq(field, _)).getOrElse(EmptyExpression)
  }
}

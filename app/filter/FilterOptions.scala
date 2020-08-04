package filter

case class FilterOptions(condition: FilterExpression, limit: Int, offset: Int)

object FilterOptions {

  def apply(
      condition: FilterExpression,
      limit: Option[Int] = None,
      offset: Option[Int] = None): FilterOptions = {
    new FilterOptions(condition, limit.getOrElse(100), offset.getOrElse(0))
  }
}

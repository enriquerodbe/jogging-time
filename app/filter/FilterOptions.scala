package filter

case class FilterOptions[F[_] <: Field[_]](condition: FilterExpression[F], limit: Int, offset: Int)

object FilterOptions {

  def apply[F[_] <: Field[_]](
      condition: FilterExpression[F],
      limit: Option[Int] = None,
      offset: Option[Int] = None): FilterOptions[F] = {
    new FilterOptions(condition, limit.getOrElse(100), offset.getOrElse(0))
  }
}

package filter.parser

import filter.Field

sealed abstract case class FieldParser[T, F[_] <: Field[_]](
    name: String,
    field: F[T],
    parser: Parser[T],
)

object FieldParser {
  def parser[T, F[_] <: Field[_]](
      name: String,
      field: F[T])(
      implicit parser: Parser[T]
  ): FieldParser[T, F] = new FieldParser(name, field, parser.parse) {}
}

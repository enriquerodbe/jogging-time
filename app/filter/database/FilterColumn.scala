package filter.database

import slick.ast.BaseTypedType
import slick.lifted.Rep

final case class FilterColumn[T](rep: Rep[T], btt: BaseTypedType[T])

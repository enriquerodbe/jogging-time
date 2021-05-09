package filter.database

import filter.Field
import slick.ast.BaseTypedType
import slick.lifted.Rep

trait FilterTable[F[_] <: Field[_]] {

  def getFilterColumn[T](field: F[T]): FilterColumn[T]

  implicit def rep2FilterColumn[T](
      rep: Rep[T])(
      implicit btt: BaseTypedType[T]
  ): FilterColumn[T] = FilterColumn(rep, btt)
}

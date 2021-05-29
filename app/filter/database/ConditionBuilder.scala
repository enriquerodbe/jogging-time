package filter.database

import filter.FilterExpression._
import filter.{Field, FilterExpression}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

class ConditionBuilder[F[_] <: Field[_]] @Inject() (val dbConfigProvider: DatabaseConfigProvider) {

  val profile = dbConfigProvider.get[JdbcProfile].profile
  import profile.api._

  def apply(filter: FilterExpression[F])(table: FilterTable[F]): Rep[Boolean] =
    filter match {
      case Empty() =>
        true
      case And(e1, e2) =>
        apply(e1)(table) && apply(e2)(table)
      case Or(e1, e2) =>
        apply(e1)(table) || apply(e2)(table)
      case Eq(field, value) =>
        val column = table.getFilterColumn(field)
        implicit val btt = column.btt
        column.rep === value
      case Ne(field, value) =>
        val queryable = table.getFilterColumn(field)
        implicit val btt = queryable.btt
        queryable.rep =!= value
      case Gt(field, value) =>
        val queryable = table.getFilterColumn(field)
        implicit val btt = queryable.btt
        queryable.rep > value
      case Lt(field, value) =>
        val queryable = table.getFilterColumn(field)
        implicit val btt = queryable.btt
        queryable.rep < value
    }

}

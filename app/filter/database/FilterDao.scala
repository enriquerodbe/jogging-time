package filter.database

import filter.FilterExpression._
import filter.{Field, FilterExpression}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait FilterDao[F[_] <: Field[_]] extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  protected def buildCondition(filter: FilterExpression[F])(table: FilterTable[F]): Rep[Boolean] =
    filter match {
      case Empty() =>
        true
      case And(e1, e2) =>
        buildCondition(e1)(table) && buildCondition(e2)(table)
      case Or(e1, e2) =>
        buildCondition(e1)(table) || buildCondition(e2)(table)
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

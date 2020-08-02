package util.filter

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait FilterDao extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  protected def buildCondition(
      filter: FilterExpression,
      table: FilterTable): Rep[Boolean] = filter match {
    case And(e1, e2) =>
      buildCondition(e1, table) && buildCondition(e2, table)
    case Or(e1, e2) =>
      buildCondition(e1, table) || buildCondition(e2, table)
    case StringEq(field, value) =>
      table.getColumn(field) === value
    case StringNe(field, value) =>
      table.getColumn(field) =!= value
    case EmptyExpression =>
      true
  }
}

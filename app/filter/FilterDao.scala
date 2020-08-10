package filter

import domain.{Distance, Speed, UserRole}
import domain.UserRole.UserRole
import java.time.{Duration, Instant}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait FilterDao extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val distanceMapper =
    MappedColumnType.base[Distance, Int](_.value, Distance)
  implicit val durationMapper =
    MappedColumnType.base[Duration, Long](_.toSeconds, Duration.ofSeconds)
  implicit val speedMapper =
    MappedColumnType.base[Speed, Double](_.value, Speed)
  implicit val rolesMapper =
    MappedColumnType.base[Set[UserRole], String](
      _.mkString(","),
      _.split(",").filterNot(_.isEmpty).map(UserRole.withName).toSet
    )

  protected def buildCondition(
      filter: FilterExpression,
      table: FilterTable): Rep[Boolean] = filter match {
    case And(e1, e2) =>
      buildCondition(e1, table) && buildCondition(e2, table)
    case Or(e1, e2) =>
      buildCondition(e1, table) || buildCondition(e2, table)
    case eq: Eq[_] =>
      buildEqCondition(eq, table)
    case ne: Ne[_] =>
      buildNeCondition(ne, table)
    case gt: Gt[_] =>
      buildGtCondition(gt, table)
    case lt: Lt[_] =>
      buildLtCondition(lt, table)
  }
  
  private def buildEqCondition(eq: Eq[_], table: FilterTable): Rep[Boolean] = {
    eq match {
      case Eq(field: StringField, value: String) =>
        table.getColumn(field) === value
      case Eq(field: InstantField, value: Instant) =>
        table.getColumn(field) === value
      case Eq(field: DistanceField, value: Distance) =>
        table.getColumn(field) === value
      case Eq(field: DurationField, value: Duration) =>
        table.getColumn(field) === value
      case Eq(field: SpeedField, value: Speed) =>
        table.getColumn(field) === value
      case Eq(field: DoubleField, value: Double) =>
        table.getColumn(field) === value
      case _ =>
        false
    }
  }
  
  private def buildNeCondition(ne: Ne[_], table: FilterTable): Rep[Boolean] = {
    ne match {
      case Ne(field: StringField, value: String) =>
        table.getColumn(field) =!= value
      case Ne(field: InstantField, value: Instant) =>
        table.getColumn(field) =!= value
      case Ne(field: DistanceField, value: Distance) =>
        table.getColumn(field) =!= value
      case Ne(field: DurationField, value: Duration) =>
        table.getColumn(field) =!= value
      case Ne(field: SpeedField, value: Speed) =>
        table.getColumn(field) =!= value
      case Ne(field: DoubleField, value: Double) =>
        table.getColumn(field) =!= value
      case _ =>
        false
    }
  }
  
  private def buildGtCondition(gt: Gt[_], table: FilterTable): Rep[Boolean] = {
    gt match {
      case Gt(field: StringField, value: String) =>
        table.getColumn(field) > value
      case Gt(field: InstantField, value: Instant) =>
        table.getColumn(field) > value
      case Gt(field: DistanceField, value: Distance) =>
        table.getColumn(field) > value
      case Gt(field: DurationField, value: Duration) =>
        table.getColumn(field) > value
      case Gt(field: SpeedField, value: Speed) =>
        table.getColumn(field) > value
      case Gt(field: DoubleField, value: Double) =>
        table.getColumn(field) > value
      case _ =>
        false
    }
  }
  
  private def buildLtCondition(lt: Lt[_], table: FilterTable): Rep[Boolean] = {
    lt match {
      case Lt(field: StringField, value: String) =>
        table.getColumn(field) < value
      case Lt(field: InstantField, value: Instant) =>
        table.getColumn(field) < value
      case Lt(field: DistanceField, value: Distance) =>
        table.getColumn(field) < value
      case Lt(field: DurationField, value: Duration) =>
        table.getColumn(field) < value
      case Lt(field: SpeedField, value: Speed) =>
        table.getColumn(field) < value
      case Lt(field: DoubleField, value: Double) =>
        table.getColumn(field) < value
      case _ =>
        false
    }
  }
}

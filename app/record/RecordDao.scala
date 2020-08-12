package record

import domain.WeekReportField.{AverageSpeed, TotalDistance, WeekOfYear}
import domain._
import filter.FilterExpression.{And, Empty, Eq, Gt, Lt, Ne, Or}
import filter.{FilterExpression, FilterOptions}
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext

@Singleton
class RecordDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext) extends RecordsTable {

  import profile.api._

  private val week = SimpleFunction.unary[Instant, Int]("week")
  private val div = SimpleBinaryOperator[Double]("/")

  def create(record: Record): DBIO[Record] = recordsInsert += record

  def retrieve(
      maybeUserId: Option[Long],
      filter: FilterOptions): DBIO[Seq[Record]] = {
    records
      .filterOpt(maybeUserId)(_.userId === _)
      .filter(buildCondition(filter.condition, _))
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  def count(userId: Option[Long], filter: FilterOptions): DBIO[Int] = {
    records
      .filterOpt(userId)(_.userId === _)
      .filter(buildCondition(filter.condition, _))
      .length
      .result
  }

  def retrieveAverages(
      userIds: Seq[Long],
      days: Int): DBIO[Seq[AverageReport]] = {
    records
      .filter { record =>
        record.date > Instant.now().minus(days.longValue, ChronoUnit.DAYS) &&
        (record.userId inSet userIds)
      }
      .groupBy(_.userId)
      .map { case (userId, records) =>
        val speeds = records.map(r => div(r.distance.asColumnOf[Double], r.duration))
        val distances = records.map(_.distance)
        (userId, speeds.avg, distances.sum)
      }
      .result
      .map(_.map((AverageReport.fromRow _).tupled))
  }

  def retrieveReport(userId: Option[Long], filter: FilterOptions) = {
    retrieveReportQuery(userId, filter)
      .drop(filter.offset)
      .take(filter.limit)
      .result
      .map(_.map(t => WeekReport.fromRow(t._1, t._2, t._3)))
  }

  private def retrieveReportQuery(
      userId: Option[Long],
      filter: FilterOptions) = {
    records
      .filterOpt(userId)(_.userId === _)
      .groupBy(r => week(r.date))
      .map { case (week, weekRecords) =>
        val speeds =
          weekRecords.map(r => div(r.distance.asColumnOf[Double], r.duration))
        val distances = weekRecords.map(_.distance)
        (week, speeds.avg, distances.sum)
      }
      .filter((buildReportCondition(filter.condition) _).tupled)
  }

  private def buildReportCondition(
      filter: FilterExpression)(
      week: Rep[Int],
      avgSpeed: Rep[Option[Double]],
      distance: Rep[Option[Distance]]): Rep[Option[Boolean]] = filter match {
    case Empty =>
      Some(true)
    case And(e1, e2) =>
      buildReportCondition(e1)(week, avgSpeed, distance) &&
        buildReportCondition(e2)(week, avgSpeed, distance)
    case Or(e1, e2) =>
      buildReportCondition(e1)(week, avgSpeed, distance) ||
        buildReportCondition(e2)(week, avgSpeed, distance)
    case Eq(WeekOfYear, value: Int) => week.? === value
    case Eq(AverageSpeed, Speed(value)) => avgSpeed  === value
    case Eq(TotalDistance, value: Distance) => distance === value
    case Ne(WeekOfYear, value: Int) => week.? =!= value
    case Ne(AverageSpeed, Speed(value)) => avgSpeed =!= value
    case Ne(TotalDistance, value: Distance) => distance =!= value
    case Gt(WeekOfYear, value: Int) => week.? > value
    case Gt(AverageSpeed, Speed(value)) => avgSpeed > value
    case Gt(TotalDistance, value: Distance) => distance > value
    case Lt(WeekOfYear, value: Int) => week.? < value
    case Lt(AverageSpeed, Speed(value)) => avgSpeed < value
    case Lt(TotalDistance, value: Distance) => distance < value
    case _ => Some(false)
  }

  def countReport(userId: Option[Long], filter: FilterOptions): DBIO[Int] = {
    retrieveReportQuery(userId, filter).length.result
  }

  def update(record: Record): DBIO[Int] = {
    records
      .filter(r => r.id === record.id && r.userId === record.userId)
      .map(r => (r.date, r.distance, r.duration, r.location))
      .update((record.date, record.distance, record.duration, record.location))
  }

  def delete(id: Long): DBIO[Int] = records.filter(_.id === id).delete

  def delete(userId: Long, recordId: Long): DBIO[Int] = {
    records.filter(r => r.id === recordId && r.userId === userId).delete
  }
}

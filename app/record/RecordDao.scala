package record

import domain._
import filter.FilterExpression._
import filter.{FilterExpression, FilterOptions}
import java.time.Instant
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import record.WeekReportField.{AverageSpeed, TotalDistance}
import scala.concurrent.ExecutionContext

@Singleton
class RecordDao @Inject() (val dbConfigProvider: DatabaseConfigProvider)(
    implicit ec: ExecutionContext
) extends RecordsTable {

  import profile.api._

  private val week = SimpleFunction.unary[Instant, Int]("week")
  private val year = SimpleFunction.unary[Instant, Int]("year")
  private val div = SimpleBinaryOperator[Double]("/")

  def create(record: Record): DBIO[Record] = recordsInsert += record

  def retrieve(userId: Option[Long], filter: FilterOptions[RecordField]): DBIO[Seq[Record]] = {
    retrieveQuery(userId, filter)
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  private def retrieveQuery(userId: Option[Long], filter: FilterOptions[RecordField]) = {
    records
      .filterOpt(userId)(_.userId === _)
      .filter(buildCondition(filter.condition))
  }

  def count(userId: Option[Long], filter: FilterOptions[RecordField]): DBIO[Int] = {
    retrieveQuery(userId, filter).length.result
  }

  def retrieveReport(
      userId: Option[Long],
      filter: FilterOptions[WeekReportField],
  ): DBIO[Seq[WeekReport]] = {
    retrieveReportQuery(userId, filter)
      .drop(filter.offset)
      .take(filter.limit)
      .result
      .map(_.map((WeekReport.fromRow _).tupled))
  }

  private def retrieveReportQuery(userId: Option[Long], filter: FilterOptions[WeekReportField]) = {
    records
      .filterOpt(userId)(_.userId === _)
      .groupBy(r => (year(r.date), week(r.date)))
      .map { case ((year, week), weekRecords) =>
        val speeds =
          weekRecords.map(r => div(r.distance.asColumnOf[Double], r.duration))
        val distances = weekRecords.map(_.distance)
        (year, week, speeds.avg, distances.sum)
      }
      .filter { case (_, _, avgSpeed, distance) =>
        buildReportCondition(filter.condition, avgSpeed, distance)
      }
  }

  private def buildReportCondition(
      filter: FilterExpression[WeekReportField],
      avgSpeed: Rep[Option[Double]],
      distance: Rep[Option[Distance]],
  ): Rep[Option[Boolean]] = filter match {
    case Empty() =>
      Some(true)
    case And(e1, e2) =>
      buildReportCondition(e1, avgSpeed, distance) &&
        buildReportCondition(e2, avgSpeed, distance)
    case Or(e1, e2) =>
      buildReportCondition(e1, avgSpeed, distance) ||
        buildReportCondition(e2, avgSpeed, distance)
    case Eq(field, Speed(value)) if field == AverageSpeed => avgSpeed === value
    case Eq(field, value: Distance) if field == TotalDistance => distance === value
    case Ne(field, Speed(value)) if field == AverageSpeed => avgSpeed =!= value
    case Ne(field, value: Distance) if field == TotalDistance => distance =!= value
    case Gt(field, Speed(value)) if field == AverageSpeed => avgSpeed > value
    case Gt(field, value: Distance) if field == TotalDistance => distance > value
    case Lt(field, Speed(value)) if field == AverageSpeed => avgSpeed < value
    case Lt(field, value: Distance) if field == TotalDistance => distance < value
    case _ => Some(false)
  }

  def countReport(userId: Option[Long], filter: FilterOptions[WeekReportField]): DBIO[Int] = {
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

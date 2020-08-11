package record

import domain._
import filter.FilterOptions
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

  private val div = SimpleBinaryOperator.apply[Double]("/")

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
        (userId, speeds.avg, distances.avg)
      }
      .result
      .map(_.map((AverageReport.fromRow _).tupled))
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

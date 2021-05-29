package record

import domain.{Distance, Speed, WeekReport}
import filter.FilterOptions
import filter.database.{ConditionBuilder, FilterColumn, FilterTable}
import java.time.Instant
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import record.WeekReportField.{AverageSpeed, TotalDistance}
import scala.concurrent.ExecutionContext

class ReportDao @Inject() (
    val dbConfigProvider: DatabaseConfigProvider,
    reportConditionBuilder: ConditionBuilder[WeekReportField],
)(implicit ec: ExecutionContext)
    extends RecordsTable {

  import profile.api._

  private val week = SimpleFunction.unary[Instant, Int]("week")
  private val year = SimpleFunction.unary[Instant, Int]("year")
  private val div = SimpleBinaryOperator[Speed]("/")

  def retrieveReport(
      userId: Option[Long],
      filter: FilterOptions[WeekReportField],
  ): DBIO[Seq[WeekReport]] = {
    retrieveReportQuery(userId, filter)
      .drop(filter.offset)
      .take(filter.limit)
      .result
      .map(_.map((WeekReport.apply _).tupled))
  }

  private def retrieveReportQuery(userId: Option[Long], filter: FilterOptions[WeekReportField]) = {
    records
      .filterOpt(userId)(_.userId === _)
      .groupBy(r => (year(r.date), week(r.date)))
      .map { case ((year, week), weekRecords) =>
        val speeds = weekRecords.map(r => div(r.distance.asColumnOf[Double], r.duration))
        val distances = weekRecords.map(_.distance)
        (year, week, speeds.avg, distances.sum)
      }
      .filter { case (_, _, avgSpeed, totalDistance) =>
        reportConditionBuilder(filter.condition)(reportTable(avgSpeed, totalDistance))
      }
  }

  private def reportTable(avgSpeed: Rep[Option[Speed]], totalDistance: Rep[Option[Distance]]) =
    new FilterTable[WeekReportField] {

      override def getFilterColumn[T](field: WeekReportField[T]): FilterColumn[T] =
        field match {
          case AverageSpeed => avgSpeed.asColumnOf[Speed]
          case TotalDistance => totalDistance.asColumnOf[Distance]
        }

    }

  def countReport(userId: Option[Long], filter: FilterOptions[WeekReportField]): DBIO[Int] = {
    retrieveReportQuery(userId, filter).length.result
  }

}

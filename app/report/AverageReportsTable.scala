package report

import domain.AverageReport.{AverageDistance, AverageSpeed, Date}
import domain.{AverageReport, AverageReportField, Distance, Speed}
import filter.{Field, FilterDao, FilterTable}
import java.time.Instant
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait AverageReportsTable
  extends HasDatabaseConfigProvider[JdbcProfile] with FilterDao {

  import profile.api._

  class AverageReports(tag: Tag)
    extends Table[AverageReport](tag, "average_reports") with FilterTable {

    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def userId = column[Long]("userId")
    def avgSpeed = column[Speed]("avgSpeed")
    def avgDistance = column[Distance]("avgDistance")
    def date = column[Instant]("date")

    def * = {
      (id, userId, avgSpeed, avgDistance, date)
        .<>((AverageReport.apply _).tupled, AverageReport.unapply)
    }

    override def getColumn[T](field: Field[T]): Rep[T] = field match {
      case f: AverageReportField[_] => (f match {
        case AverageSpeed => avgSpeed
        case AverageDistance => avgDistance
        case Date => date
      }).asInstanceOf[Rep[T]]
    }
  }

  val averageReports = TableQuery[AverageReports]
  val averageReportsInsert =
    averageReports
      .returning(averageReports.map(_.id))
      .into((r, id) => r.copy(id = id))
}

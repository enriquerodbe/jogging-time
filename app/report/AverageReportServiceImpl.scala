package report

import domain.{AverageReport, Page}
import filter.FilterOptions
import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import record.RecordService
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile

@Singleton
private[report] class AverageReportServiceImpl @Inject()(
    val dbConfigProvider: DatabaseConfigProvider,
    recordService: RecordService,
    averageReportDao: AverageReportDao)(
    implicit ec: ExecutionContext)
  extends AverageReportService with HasDatabaseConfigProvider[JdbcProfile] {

  override def create(days: Int): Future[Seq[AverageReport]] = {
    for {
      userIds <- db.run(averageReportDao.retrieveUsersWithNoReportInDays(days))
      averages <- recordService.retrieveAverages(userIds, 7)
      reports <- db.run(averageReportDao.createReports(averages))
    } yield reports
  }

  override def retrieve(
      userId: Option[Long],
      filter: FilterOptions): Future[Page[AverageReport]] = {
    val query = for {
      total <- averageReportDao.count(userId, filter.condition)
      results <- averageReportDao.retrieve(userId, filter)
    } yield Page(results, total, results.size, filter.offset)

    db.run(query)
  }
}

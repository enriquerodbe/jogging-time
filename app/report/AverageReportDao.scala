package report

import domain.AverageReport
import filter.{FilterExpression, FilterOptions}
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import user.UsersTable

@Singleton
class AverageReportDao @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)
  extends AverageReportsTable with UsersTable {

  import profile.api._

  def retrieveUsersWithNoReportInDays(days: Int): DBIO[Seq[Long]] = {
    val daysAgo = Instant.now().minus(days.longValue, ChronoUnit.DAYS)
    users
      .joinLeft(averageReports).on(_.id === _.userId)
      .groupBy { case (user, _) => user.id }
      .map { case (userId, reports) =>
        (userId, reports.map(_._2.map(_.date)).max)
      }
      .filter { case (_, maxDate) => maxDate.isEmpty || maxDate < daysAgo }
      .map(_._1)
      .result
  }

  def createReports(reports: Seq[AverageReport]): DBIO[Seq[AverageReport]] = {
    averageReportsInsert ++= reports
  }

  def retrieve(
      userId: Option[Long],
      filter: FilterOptions): DBIO[Seq[AverageReport]] = {
    averageReports
      .filterOpt(userId)(_.userId === _)
      .filter(buildCondition(filter.condition, _))
      .drop(filter.offset)
      .take(filter.limit)
      .result
  }

  def count(userId: Option[Long], filter: FilterExpression): DBIO[Int] = {
    averageReports
      .filterOpt(userId)(_.userId === _)
      .filter(buildCondition(filter, _))
      .length
      .result
  }
}

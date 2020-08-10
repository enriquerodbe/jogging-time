package report

import akka.actor.Actor
import com.typesafe.config.Config
import javax.inject.Inject
import play.api.Logger
import report.AverageReportsActor.CreateAverageReports
import scala.concurrent.ExecutionContext

class AverageReportsActor @Inject()(
    config: Config,
    averageReportService: AverageReportService)(
    implicit ec: ExecutionContext) extends Actor {

  private val logger = Logger(getClass)

  private val missingDays = config.getInt("reports.average.missing.days")

  override def receive: Receive = {
    case CreateAverageReports =>
      averageReportService
        .create(missingDays)
        .foreach(r => logger.debug(s"Generated reports: ${r.mkString(" | ")}"))
  }
}

object AverageReportsActor {
  case object CreateAverageReports
}

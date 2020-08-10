package report

import com.google.inject.ImplementedBy
import domain.{AverageReport, Page}
import filter.FilterOptions
import scala.concurrent.Future

@ImplementedBy(classOf[AverageReportServiceImpl])
trait AverageReportService {

  def create(days: Int): Future[Seq[AverageReport]]

  def retrieve(
      userId: Option[Long],
      filter: FilterOptions): Future[Page[AverageReport]]
}

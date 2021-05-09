package record

import com.google.inject.ImplementedBy
import domain.{Page, Record, WeekReport}
import filter.FilterOptions
import scala.concurrent.Future

@ImplementedBy(classOf[RecordServiceImpl])
trait RecordService {

  def create(record: Record): Future[Record]

  def retrieve(
      maybeUserId: Option[Long],
      filterOptions: FilterOptions[RecordField]): Future[Page[Record]]

  def retrieveReport(
      userId: Option[Long],
      filter: FilterOptions[WeekReportField]): Future[Page[WeekReport]]

  def update(updatedRecord: Record): Future[Unit]

  def delete(recordId: Long): Future[Unit]

  def delete(userId: Long, recordId: Long): Future[Unit]
}

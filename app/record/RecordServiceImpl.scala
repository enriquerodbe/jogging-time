package record

import domain.{Page, Record}
import filter.{EmptyExpression, FilterOptions}
import javax.inject.Inject
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.JdbcProfile
import weather.WeatherService

private[record] class RecordServiceImpl @Inject()(
    val dbConfigProvider: DatabaseConfigProvider,
    weatherService: WeatherService,
    recordDao: RecordDao)(
    implicit ec: ExecutionContext)
  extends RecordService with HasDatabaseConfigProvider[JdbcProfile] {

  private lazy val logger = Logger(getClass)

  override def create(record: Record): Future[Record] = {
    val weatherRequest =
      weatherService
        .retrieve(record.location, record.date)
        .map(Some(_))
        .recover {
          case ex =>
            logger.warn(s"Error retrieving weather conditions for $record", ex)
            None
        }

    for {
      weatherResult <- weatherRequest
      result <- db.run(recordDao.create(record.copy(weather = weatherResult)))
    } yield result
  }

  override def retrieve(userId: Long): Future[Page[Record]] = {
    val filter = FilterOptions(EmptyExpression)
    val query = for {
      results <- recordDao.retrieve(userId, filter)
      total <- recordDao.count(userId, filter)
    } yield Page(results, total, results.size, filter.offset)

    db.run(query)
  }

  override def retrieveAll(maybeUserId: Option[Long]): Future[Page[Record]] = {
    val filter = FilterOptions(EmptyExpression)
    val query = for {
      results <- recordDao.retrieve(filter)
      total <- recordDao.count(filter)
    } yield Page(results, total, results.size, filter.offset)

    db.run(query)
  }

  override def update(record: Record): Future[Unit] = {
    db.run(recordDao.update(record)).map(_ => ())
  }

  override def delete(recordId: Long): Future[Unit] = {
    db.run(recordDao.delete(recordId)).map(_ => ())
  }

  override def delete(userId: Long, recordId: Long): Future[Unit] = {
    db.run(recordDao.delete(userId, recordId)).map(_ => ())
  }
}

package record

import domain.{Distance, Location, Record, RecordField, WeatherConditions}
import filter.{Field, FilterDao, FilterTable}
import java.time.{Duration, Instant}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait RecordsTable
  extends HasDatabaseConfigProvider[JdbcProfile] with FilterDao {

  import profile.api._

  class Records(tag: Tag)
    extends Table[Record](tag, "records") with FilterTable {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("userId")
    def date = column[Instant]("date")
    def distance = column[Distance]("distance")
    def duration = column[Duration]("duration")
    def locationLat = column[Double]("locationLat")
    def locationLon = column[Double]("locationLon")
    def temperature = column[Option[BigDecimal]]("temperature")
    def humidity = column[Option[Int]]("humidity")
    def windSpeed = column[Option[BigDecimal]]("windSpeed")

    def location = {
      (locationLat, locationLon).<>(Location.tupled, Location.unapply)
    }

    def weather = {
      (temperature, humidity, windSpeed)
        .<>((WeatherConditions.fromValues _).tupled, WeatherConditions.toTuple)
    }

    def * = {
      (id, userId, date, distance, duration, location, weather)
        .<>(Record.tupled, Record.unapply)
    }

    override def getColumn[T](field: Field[T]): Rep[T] = {
      (field match {
        case RecordField.Date => date
        case RecordField.Distance => distance
        case RecordField.Duration => duration
        case RecordField.LocationLat => locationLat
        case RecordField.LocationLon => locationLon
      }).asInstanceOf[Rep[T]]
    }
  }

  val records = TableQuery[Records]

  val recordsInsert =
    records.returning(records.map(_.id)).into((r, id) => r.copy(id = id))
}

package record

import domain.{Distance, Location, Record, WeatherConditions}
import filter.{Field, FilterTable}
import java.time.{Duration, Instant}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

private[record] trait RecordsTable
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val distanceMapper =
    MappedColumnType.base[Distance, Int](_.value, Distance)
  implicit val durationMapper =
    MappedColumnType.base[Duration, Long](_.toSeconds, Duration.ofSeconds)

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

    override def getColumn[T](field: Field[T]): Rep[T] = ???
  }

  val records = TableQuery[Records]

  val recordsInsert =
    records.returning(records.map(_.id)).into((r, id) => r.copy(id = id))
}

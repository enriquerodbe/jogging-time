package record

import domain._
import filter.database.{FilterColumn, FilterTable}
import java.time.{Duration, Instant}
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait RecordsTable extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  implicit val distanceMapper: BaseColumnType[Distance] =
    MappedColumnType.base[Distance, Int](_.value, Distance)

  implicit val durationMapper: BaseColumnType[Duration] =
    MappedColumnType.base[Duration, Long](_.toSeconds, Duration.ofSeconds)

  implicit val speedMapper: BaseColumnType[Speed] =
    MappedColumnType.base[Speed, Double](_.value, Speed)

  class Records(tag: Tag) extends Table[Record](tag, "records") with FilterTable[RecordField] {

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

    def location = (locationLat, locationLon) <> (Location.tupled, Location.unapply)

    private def weatherConditionsFromValues(
        maybeTemperature: Option[BigDecimal],
        maybeHumidity: Option[Int],
        maybeWindSpeed: Option[BigDecimal],
    ) = (maybeTemperature, maybeHumidity, maybeWindSpeed) match {
      case (Some(temperature), Some(humidity), Some(windSpeed)) =>
        Some(WeatherConditions(temperature, humidity, windSpeed))
      case _ => None
    }

    private def weatherConditionsToTuple(weather: Option[WeatherConditions]) =
      Some((weather.map(_.temperature), weather.map(_.humidity), weather.map(_.windSpeed)))

    def weather =
      (temperature, humidity, windSpeed)
        .<>((weatherConditionsFromValues _).tupled, weatherConditionsToTuple)

    def * =
      (id, userId, date, distance, duration, location, weather) <> (Record.tupled, Record.unapply)

    override def getFilterColumn[T](field: RecordField[T]): FilterColumn[T] =
      field match {
        case RecordField.Date => date
        case RecordField.Distance => distance
        case RecordField.Duration => duration
        case RecordField.LocationLat => locationLat
        case RecordField.LocationLon => locationLon
      }

  }

  val records = TableQuery[Records]

  val recordsInsert =
    records.returning(records.map(_.id)).into((r, id) => r.copy(id = id))

}

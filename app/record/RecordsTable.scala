package record

import domain._
import filter.database.{FilterDao, FilterTable, FilterColumn}
import java.time.{Duration, Instant}

trait RecordsTable extends FilterDao[RecordField] {

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

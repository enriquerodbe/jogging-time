package domain

import filter._
import java.time.Instant

case class Record(
    id: Long,
    userId: Long,
    date: Instant,
    distance: Distance,
    duration: java.time.Duration,
    location: Location,
    weather: Option[WeatherConditions],
)

sealed trait RecordField[T] extends Field[T]

object RecordField {
  case object Date extends InstantField with RecordField[Instant]
  case object Distance extends DistanceField with RecordField[Distance]
  case object Duration extends DurationField with RecordField[java.time.Duration]
  case object LocationLat extends DoubleField with RecordField[Double]
  case object LocationLon extends DoubleField with RecordField[Double]
}

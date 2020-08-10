package domain

import filter.{DistanceField, Field, InstantField, SpeedField}
import java.time.Instant

case class AverageReport(
    id: Long,
    userId: Long,
    averageSpeed: Speed,
    averageDistance: Distance,
    date: Instant,
)

sealed trait AverageReportField[T] extends Field[T]

object AverageReport {

  case object AverageSpeed extends SpeedField with AverageReportField[Speed]
  case object AverageDistance extends DistanceField with AverageReportField[Distance]
  case object Date extends InstantField with AverageReportField[Instant]

  def fromRow(
      userId: Long,
      maybeAverageSpeed: Option[Double],
      maybeAverageDistance: Option[Distance]): AverageReport = {
    AverageReport(
      -1,
      userId,
      Speed(maybeAverageSpeed.getOrElse(0)),
      maybeAverageDistance.getOrElse(Distance(0)),
      Instant.now())
  }
}

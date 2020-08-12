package domain

import filter.{DistanceField, Field, SpeedField}

case class WeekReport(
    year: Int,
    weekOfYear: Int,
    averageSpeed: Option[Speed],
    total: Option[Distance])

object WeekReport {

  def fromRow(
      year: Int,
      weekOfYear: Int,
      averageSpeed: Option[Double],
      totalDistance: Option[Distance]) = {
    WeekReport(year, weekOfYear, averageSpeed.map(Speed), totalDistance)
  }
}

sealed trait WeekReportField[T] extends Field[T]

object WeekReportField {
  case object AverageSpeed extends SpeedField with WeekReportField[Speed]
  case object TotalDistance extends DistanceField with WeekReportField[Distance]
}

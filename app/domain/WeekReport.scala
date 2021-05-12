package domain

case class WeekReport(
    year: Int,
    weekOfYear: Int,
    averageSpeed: Option[Speed],
    total: Option[Distance],
)

object WeekReport {

  def fromRow(
      year: Int,
      weekOfYear: Int,
      averageSpeed: Option[Double],
      totalDistance: Option[Distance],
  ): WeekReport = {
    WeekReport(year, weekOfYear, averageSpeed.map(Speed), totalDistance)
  }

}

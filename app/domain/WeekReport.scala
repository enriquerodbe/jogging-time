package domain

case class WeekReport(
    year: Int,
    weekOfYear: Int,
    averageSpeed: Option[Speed],
    total: Option[Distance],
)

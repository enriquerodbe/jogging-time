package record

import domain.{Distance, Speed}
import filter.Field

sealed trait WeekReportField[T] extends Field[T]

object WeekReportField {
  case object AverageSpeed extends WeekReportField[Speed]
  case object TotalDistance extends WeekReportField[Distance]
}

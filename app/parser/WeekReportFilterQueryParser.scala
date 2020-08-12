package parser

import domain.WeekReportField.{AverageSpeed, TotalDistance}
import filter.Field
import javax.inject.Inject

class WeekReportFilterQueryParser @Inject() extends BaseParser {

  private def avgSpeed = """avgSpeed""".r ^^ { _ => AverageSpeed }
  private def totalDistance = """distance""".r ^^ { _ => TotalDistance }

  override protected def fields: Seq[Parser[Field[_]]] = {
    Seq(avgSpeed, totalDistance)
  }
}

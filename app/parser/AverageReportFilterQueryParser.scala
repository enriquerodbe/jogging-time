package parser

import domain.AverageReport.{AverageDistance, AverageSpeed, Date}
import filter.Field
import javax.inject.Inject

class AverageReportFilterQueryParser @Inject() extends BaseParser {

  private def avgSpeed = """avgSpeed""".r ^^ { _ => AverageSpeed }
  private def avgDistance = """avgDistance""".r ^^ { _ => AverageDistance }
  private def date = """date""".r ^^ { _ => Date }

  override protected def fields: Seq[Parser[Field[_]]] = {
    Seq(avgSpeed, avgDistance, date)
  }
}

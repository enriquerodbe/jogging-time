package record

import filter.parser.FieldParser.parser
import filter.parser.{BaseParser, FieldParser}
import javax.inject.Inject
import record.Parser.Instances._
import record.WeekReportField.{AverageSpeed, TotalDistance}

class WeekReportFilterQueryParser @Inject() extends BaseParser[WeekReportField] {

  override protected val fields: Seq[FieldParser[_, WeekReportField]] =
    Seq(
      parser("avgSpeed", AverageSpeed),
      parser("distance", TotalDistance),
    )

}

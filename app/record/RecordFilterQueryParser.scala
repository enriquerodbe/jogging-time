package record

import filter.parser.FieldParser.parser
import filter.parser.Parser.Instances._
import filter.parser.{BaseParser, FieldParser}
import javax.inject.Inject
import record.Parser.Instances.distanceParser

class RecordFilterQueryParser @Inject() extends BaseParser[RecordField] {

  override protected val fields: Seq[FieldParser[_, RecordField]] = {
    Seq(
      parser("date", RecordField.Date),
      parser("distance", RecordField.Distance),
      parser("duration", RecordField.Duration),
      parser("lat", RecordField.LocationLat),
      parser("lon", RecordField.LocationLon),
    )
  }

}

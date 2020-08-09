package parser

import domain.RecordField._
import filter.Field
import javax.inject.Inject

class RecordFilterQueryParser @Inject() extends BaseParser {

  private def date = """date""".r ^^ { _ => Date }
  private def distance = """distance""".r ^^ { _ => Distance }
  private def duration = """duration""".r ^^ { _ => Duration }
  private def lat = """lat""".r ^^ { _ => LocationLat }
  private def lon = """lon""".r ^^ { _ => LocationLon }

  override protected def fields: Seq[Parser[Field[_]]] = {
    Seq(date, distance, duration, lat, lon)
  }
}

package record

import domain.Distance
import filter.Field
import java.time.{Duration, Instant}

sealed trait RecordField[T] extends Field[T]

object RecordField {
  case object Date extends RecordField[Instant]
  case object Distance extends RecordField[Distance]
  case object Duration extends RecordField[Duration]
  case object LocationLat extends RecordField[Double]
  case object LocationLon extends RecordField[Double]
}

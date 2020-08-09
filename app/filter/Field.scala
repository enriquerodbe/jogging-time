package filter

import domain.Distance
import java.time.{Duration, Instant}

trait Field[+T] {
  def parseValue(value: String): T
}

trait StringField extends Field[String] {
  override def parseValue(value: String): String = value
}

trait InstantField extends Field[Instant] {
  override def parseValue(value: String): Instant = Instant.parse(value)
}

trait DistanceField extends Field[Distance] {
  override def parseValue(value: String): Distance = Distance(value.toInt)
}

trait DurationField extends Field[Duration] {
  override def parseValue(value: String): Duration = Duration.parse(value)
}

trait DoubleField extends Field[Double] {
  override def parseValue(value: String): Double = value.toDouble
}

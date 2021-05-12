package filter.parser

import java.time.{Duration, Instant}

trait Parser[T] {
  def parse(rawValue: String): T
}

object Parser {

  object Instances {
    implicit val stringParser: Parser[String] = identity
    implicit val intParser: Parser[Int] = _.toInt
    implicit val doubleParser: Parser[Double] = _.toDouble
    implicit val instantParser: Parser[Instant] = Instant.parse(_)
    implicit val durationParser: Parser[Duration] = Duration.parse(_)
  }

}

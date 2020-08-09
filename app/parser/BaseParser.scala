package parser

import filter._
import scala.util.Try
import scala.util.parsing.combinator.RegexParsers

trait BaseParser extends RegexParsers {

  protected def fields: Seq[Parser[Field[_]]]

  private def value = "'" ~> """[^']*""".r <~ "'" | """-?[0-9]+(\.[0-9]+)?""".r

  private def comparison: Parser[FilterExpression] = {
    fields.flatMap { field =>
      Seq(
        field ~ "eq" ~ value ^^ { case f ~ _ ~ v => Eq(f, f.parseValue(v)) },
        field ~ "ne" ~ value ^^ { case f ~ _ ~ v => Ne(f, f.parseValue(v)) },
        field ~ "gt" ~ value ^^ { case f ~ _ ~ v => Gt(f, f.parseValue(v)) },
        field ~ "lt" ~ value ^^ { case f ~ _ ~ v => Lt(f, f.parseValue(v)) },
      )
    }.reduce(_ | _)
  }

  private def factor: Parser[FilterExpression] = comparison | "(" ~> expr <~ ")"

  private def term: Parser[FilterExpression] = factor ~ rep("and" ~ factor) ^^ {
    case comparison ~ list => list.foldLeft(comparison)((x, y) => And(x, y._2))
  }

  private def expr: Parser[FilterExpression] = term ~ rep("or" ~ expr) ^^ {
    case comparison ~ list => list.foldLeft(comparison)((x, y) => Or(x, y._2))
  }

  def parse(input: String): Try[FilterExpression] = {
    parseAll(expr, input) match {
      case Success(filterExpression, _) => scala.util.Success(filterExpression)
      case Failure(msg, _) => scala.util.Failure(new Exception(msg))
      case Error(msg, _) => scala.util.Failure(new Exception(msg))
    }
  }
}

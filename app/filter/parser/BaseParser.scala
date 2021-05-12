package filter.parser

import filter.FilterExpression._
import filter._
import scala.util.Try
import scala.util.parsing.combinator.RegexParsers

trait BaseParser[F[_] <: Field[_]] extends RegexParsers {

  protected def fields: Seq[FieldParser[_, F]]

  private def value = "'" ~> """[^']*""".r <~ "'" | """-?[0-9]+(\.[0-9]+)?""".r

  private def comparison: Parser[FilterExpression[F]] = {
    fields
      .flatMap { case FieldParser(name, field, parser) =>
        Seq(
          name ~ "eq" ~ value ^^ { case _ ~ _ ~ v => Eq(field, parser.parse(v)) },
          name ~ "ne" ~ value ^^ { case _ ~ _ ~ v => Ne(field, parser.parse(v)) },
          name ~ "gt" ~ value ^^ { case _ ~ _ ~ v => Gt(field, parser.parse(v)) },
          name ~ "lt" ~ value ^^ { case _ ~ _ ~ v => Lt(field, parser.parse(v)) },
        )
      }
      .reduce(_ | _)
  }

  private def factor: Parser[FilterExpression[F]] = {
    comparison | "(" ~> expr <~ ")"
  }

  private def term: Parser[FilterExpression[F]] = {
    factor ~ rep("and" ~ factor) ^^ { case comparison ~ list =>
      list.foldLeft(comparison)((x, y) => And(x, y._2))
    }
  }

  private def expr: Parser[FilterExpression[F]] = term ~ rep("or" ~ expr) ^^ {
    case comparison ~ list => list.foldLeft(comparison)((x, y) => Or(x, y._2))
  }

  def parse(input: String): Try[FilterExpression[F]] = {
    parseAll(expr, input) match {
      case Success(filterExpression, _) => util.Success(filterExpression)
      case Failure(msg, _) => util.Failure(ParseException(msg))
      case Error(msg, _) => util.Failure(ParseException(msg))
    }
  }

  def parse(input: Option[String]): Try[FilterExpression[F]] = {
    input.map(parse).getOrElse(util.Success(Empty()))
  }

}

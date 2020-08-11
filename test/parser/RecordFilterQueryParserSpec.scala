package parser

import domain.RecordField.{Date, Distance, Duration, LocationLat, LocationLon}
import filter.{Eq, Gt, Lt, Ne}
import java.time
import java.time.Instant
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class RecordFilterQueryParserSpec extends AnyFlatSpec with should.Matchers {

  val parser = new RecordFilterQueryParser

  "RecordFilterQueryParserSpec" should "parse date" in {
    val expr = "date eq '2020-08-08T01:10:12Z'"
    val result = parser.parse(expr).get
    result shouldEqual Eq(Date, Instant.parse("2020-08-08T01:10:12Z"))
  }
  it should "parse distance" in {
    val expr = "distance lt 10000"
    val result = parser.parse(expr).get
    result shouldEqual Lt(Distance, domain.Distance(10000))
  }
  it should "parse duration" in {
    val expr = "duration gt 'PT60M'"
    val result = parser.parse(expr).get
    result shouldEqual Gt(Duration, time.Duration.ofMinutes(60))
  }
  it should "parse lat" in {
    val expr = "lat ne 12.77"
    val result = parser.parse(expr).get
    result shouldEqual Ne(LocationLat, 12.77)
  }
  it should "parse lon" in {
    val expr = "lon ne 50.17"
    val result = parser.parse(expr).get
    result shouldEqual Ne(LocationLon, 50.17)
  }
}

package parser

import domain.UserField.{Email, FirstName, LastName}
import filter.FilterExpression._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class UserFilterQueryParserSpec extends AnyFlatSpec with should.Matchers {

  val parser = new UserFilterQueryParser

  "A UserFilterQueryParser" should "parse gt" in {
    val expr = "firstName gt 'admin'"
    val result = parser.parse(expr).get
    result shouldEqual Gt(FirstName, "admin")
  }

  it should "parse lt" in {
    val expr = "lastName lt 'admin'"
    val result = parser.parse(expr).get
    result shouldEqual Lt(LastName, "admin")
  }

  it should "parse eq" in {
    val expr = "email eq 'admin@jogging.com'"
    val result = parser.parse(expr).get
    result shouldEqual Eq(Email, "admin@jogging.com")
  }

  it should "parse ne" in {
    val expr = "firstName ne 'admin'"
    val result = parser.parse(expr).get
    result shouldEqual Ne(FirstName, "admin")
  }

  it should "parse and" in {
    val expr = "firstName eq 'Helge' and lastName eq 'Doppler'"
    val result = parser.parse(expr).get
    result shouldEqual And(Eq(FirstName, "Helge"), Eq(LastName, "Doppler"))
  }

  it should "parse or" in {
    val expr = "firstName eq 'Helge' or lastName eq 'Doppler'"
    val result = parser.parse(expr).get
    result shouldEqual Or(Eq(FirstName, "Helge"), Eq(LastName, "Doppler"))
  }

  it should "parse and with greater precedence" in {
    val expr = "email ne 'admin@jogging.com' or " +
      "firstName eq 'Helge' and lastName eq 'Doppler'"
    val result = parser.parse(expr).get
    result shouldEqual
      Or(
        Ne(Email, "admin@jogging.com"),
        And(Eq(FirstName, "Helge"), Eq(LastName, "Doppler")))
  }

  it should "parse parentheses" in {
    val expr = "(email ne 'admin@jogging.com' or firstName eq 'Helge') " +
      "and lastName eq 'Doppler'"
    val result = parser.parse(expr).get
    result shouldEqual
      And(
        Or(Ne(Email, "admin@jogging.com"), Eq(FirstName, "Helge")),
        Eq(LastName, "Doppler"))
  }

  it should "fail on invalid filter" in {
    val expr = "invalid"
    intercept[Exception](parser.parse(expr).get)
  }
}

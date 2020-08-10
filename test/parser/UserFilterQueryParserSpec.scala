package parser

import domain.UserField.{Email, FirstName}
import filter._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class UserFilterQueryParserSpec extends AnyFlatSpec with should.Matchers {

  val parser = new UserFilterQueryParser

  "A UserFilterQueryParser" should "parse a single comparison" in {
    val result = parser
      .parse("(email eq 'claudia@dark.io' or email gt 'claudia@dark.io') and firstName gt 'admin'")
      .get

    result shouldEqual And(Or(Eq(Email, "claudia@dark.io"), Gt(Email, "claudia@dark.io")), Gt(FirstName, "admin"))
  }
}

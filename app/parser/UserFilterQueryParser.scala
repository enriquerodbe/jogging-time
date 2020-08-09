package parser

import domain.UserField
import domain.UserField.{Email, FirstName, LastName}
import javax.inject.Inject

class UserFilterQueryParser @Inject() extends BaseParser {

  private def email = """email""".r ^^ { _ => Email }
  private def firstName = """firstName""".r ^^ { _ => FirstName }
  private def lastName = """lastName""".r ^^ { _ => LastName }

  override protected def fields: Seq[Parser[UserField]] = {
    Seq(email, firstName, lastName)
  }
}

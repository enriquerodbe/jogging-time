package user

import filter.parser.FieldParser._
import filter.parser.Parser.Instances._
import filter.parser.{BaseParser, FieldParser}
import javax.inject.Inject
import user.UserField.{Email, FirstName, LastName}

class UserFilterQueryParser @Inject() extends BaseParser[UserField] {

  override protected val fields: Seq[FieldParser[_, UserField]] = {
    Seq(
      parser("email", Email),
      parser("firstName", FirstName),
      parser("lastName", LastName),
    )
  }

}

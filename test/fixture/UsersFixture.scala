package fixture

import fixture.Fixture._
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext
import user.UsersTable
import user.password.PasswordsTable

class UsersFixture @Inject() (val dbConfigProvider: DatabaseConfigProvider)(
    implicit ex: ExecutionContext
) extends UsersTable
    with PasswordsTable {

  import profile.api._

  def insertUsers(): Unit = {
    val _ = db.run {
      for {
        _ <- usersInsert ++= Seq(hannah, mikkel, jonas, helge)
        _ <- passwords ++= initialPasswords
      } yield ()
    }
  }

}

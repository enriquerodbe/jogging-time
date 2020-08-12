package fixture

import com.mohiva.play.silhouette.api.crypto.Base64
import domain.UserRole.{Admin, Manager}
import domain._
import fixture.Fixture._
import java.time.temporal.ChronoUnit.DAYS
import java.time.{Duration, Instant}
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Headers
import play.api.test.FakeRequest
import record.RecordsTable
import report.AverageReportsTable
import scala.concurrent.ExecutionContext
import user.UsersTable
import user.password.PasswordsTable

class Fixture @Inject()(
    val dbConfigProvider: DatabaseConfigProvider)(
    implicit ex: ExecutionContext)
  extends UsersTable
    with RecordsTable
    with AverageReportsTable
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
  def insertRecords(): Unit = {
    val _ = db.run(recordsInsert ++= initialRecords)
  }
}

object Fixture {

  private val fiveK = Distance(5_000)
  private val eightK = Distance(8_000)
  private val tenK = Distance(10_000)
  private val eightDaysAgo = Instant.now().minus(8, DAYS)
  private val threeDaysAgo = Instant.now().minus(3, DAYS)
  private val oneDayAgo = Instant.now().minus(1, DAYS)
  private val thirtyMin = Duration.ofMinutes(30)
  private val fortyMin = Duration.ofMinutes(40)
  private val sixtyMin = Duration.ofMinutes(60)

  val admin = User(1, "admin@jogging.com", "Admin", "Admin", Set(Admin))
  val hannah = User(2, "hannah@dark.io", "Hannah", "Kahnwald", Set(Manager))
  val mikkel = User(3, "mikkel@dark.io", "Mikkel", "Nielsen", Set.empty)
  val jonas = User(4, "jonas@dark.io", "Jonas", "Kahnwald", Set.empty)
  val helge = User(5, "helge@dark.io", "Helge", "Doppler", Set.empty)

  val location = Location(27.2038, 77.5011)

  val initialRecords = Seq(
    Record(1, hannah.id, threeDaysAgo, fiveK, thirtyMin, location, None),
    Record(2, hannah.id, threeDaysAgo, eightK, fortyMin, location, None),
    Record(3, hannah.id, threeDaysAgo, tenK, sixtyMin, location, None),
    Record(4, hannah.id, threeDaysAgo, fiveK, fortyMin, location, None),
    Record(5, mikkel.id, oneDayAgo, eightK, fortyMin, location, None),
    Record(6, mikkel.id, oneDayAgo, tenK, sixtyMin, location, None),
    Record(7, mikkel.id, oneDayAgo, fiveK, thirtyMin, location, None),
    Record(8, jonas.id, eightDaysAgo, eightK, thirtyMin, location, None),
    Record(9, jonas.id, eightDaysAgo, tenK, sixtyMin, location, None),
    Record(10, jonas.id, threeDaysAgo, fiveK, thirtyMin, location, None),
  )

  val hasher = "bcrypt"
  val hash = "$2a$10$pjnp9/GDJxEv0tGYZRHpT.esEkQsfsq/z2TY3YpnoVpsOvzT4TE3m"
  val initialPasswords = Seq(
    Password(hannah.email, hasher, hash, None),
    Password(mikkel.email, hasher, hash, None),
    Password(jonas.email, hasher, hash, None),
    Password(helge.email, hasher, hash, None),
  )

  def buildBasicAuthHeader(email: String, pass: String): Headers = {
    Headers("Authorization" -> s"Basic ${Base64.encode(s"$email:$pass")}")
  }

  val adminAuthHeader = buildBasicAuthHeader(admin.email, "test")
  val hannahAuthHeader = buildBasicAuthHeader(hannah.email, "test")

  val adminRequest = FakeRequest().withHeaders(adminAuthHeader)
}

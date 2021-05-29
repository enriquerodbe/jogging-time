package fixture

import com.mohiva.play.silhouette.api.crypto.Base64
import domain.UserRole.{Admin, Manager}
import domain._
import java.time.temporal.ChronoUnit.DAYS
import java.time.{Duration, Instant}
import play.api.mvc.Headers
import play.api.test.FakeRequest

object Fixture {

  val fiveK = Distance(5_000)
  val eightK = Distance(8_000)
  val tenK = Distance(10_000)
  val today = Instant.parse("2020-08-12T00:00:00Z")
  val eightDaysAgo = today.minus(8, DAYS)
  val threeDaysAgo = today.minus(3, DAYS)
  val oneDayAgo = today.minus(1, DAYS)
  val thirtyMin = Duration.ofMinutes(30)
  val fortyMin = Duration.ofMinutes(40)
  val sixtyMin = Duration.ofMinutes(60)

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

  def buildBasicAuthHeader(email: String, pass: String): Headers =
    Headers("Authorization" -> s"Basic ${Base64.encode(s"$email:$pass")}")

  val adminAuthHeader = buildBasicAuthHeader(admin.email, "test")
  val hannahAuthHeader = buildBasicAuthHeader(hannah.email, "test")

  val adminRequest = FakeRequest().withHeaders(adminAuthHeader)
}

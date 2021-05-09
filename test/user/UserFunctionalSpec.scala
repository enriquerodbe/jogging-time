package user

import domain.User
import domain.UserRole.{Admin, Manager}
import fixture.BaseSpec
import fixture.Fixture._
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import filter.parser.ParseException
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import user.password.PasswordDao

class UserFunctionalSpec extends BaseSpec {

  import dbConfig.profile.api._

  val userController = instanceOf[UserController]
  val userDao = instanceOf[UserDao]
  val passwordDao = instanceOf[PasswordDao]

  "create" should {
    "create new user" in {
      val ulrich = CreateUserDto("ulrich@dark.io", "test", "Ulrich", "Nielsen")
      val expected =
        User(6, ulrich.email, ulrich.firstName, ulrich.lastName, Set.empty)

      val response = userController.create()(FakeRequest().withBody(ulrich))

      status(response) mustEqual CREATED
      val jsonResponse = contentAsJson(response)
      (jsonResponse \ "id").as[Long] mustEqual expected.id
      (jsonResponse \ "email").as[String] mustEqual expected.email
      (jsonResponse \ "firstName").as[String] mustEqual expected.firstName
      (jsonResponse \ "lastName").as[String] mustEqual expected.lastName
      (jsonResponse \ "roles").as[Seq[String]] mustBe empty

      val userRecord = execute(userDao.users.filter(_.email === expected.email))
      userRecord must contain only expected

      val passwordRecord =
        execute(passwordDao.passwords.filter(_.userEmail === ulrich.email))
      passwordRecord must have size 1
      passwordRecord.head.userEmail mustEqual ulrich.email
    }

    "fail on existing email" in {
      val admin = CreateUserDto("admin@jogging.com", "test", "Admin", "Admin")

      intercept[JdbcSQLIntegrityConstraintViolationException] {
        await(userController.create()(FakeRequest().withBody(admin)))
      }
    }
  }

  "retrieve" should {
    "filter users by first name" in {
      val filter = Some("firstName eq 'Jonas'")
      val response = userController.retrieve(filter, None, None)(adminRequest)
      val responseBody = contentAsJson(response)

      (responseBody \ "count").as[Int] mustEqual 1
      (responseBody \ "results").as[Seq[JsObject]].foreach { user =>
        (user \ "firstName").as[String] mustEqual "Jonas"
      }
    }
    "filter users by last name" in {
      val filter = Some("lastName gt 'Doppler'")
      val response = userController.retrieve(filter, None, None)(adminRequest)
      val responseBody = contentAsJson(response)

      (responseBody \ "count").as[Int] mustEqual 4
      (responseBody \ "results").as[Seq[JsObject]].foreach { user =>
        (user \ "lastName").as[String] must be > "Doppler"
      }
    }

    "skip offset results" in {
      val filter = Some("lastName gt 'Doppler'")
      val limit = None
      val offset = Some(1)
      val response = userController.retrieve(filter, limit, offset)(adminRequest)
      val responseBody = contentAsJson(response)

      (responseBody \ "count").as[Int] mustEqual 3
      (responseBody \ "offset").as[Int] mustEqual 1
      (responseBody \ "results").as[Seq[JsObject]].foreach { user =>
        (user \ "lastName").as[String] must be > "Doppler"
      }
    }

    "limit results" in {
      val filter = Some("lastName gt 'Doppler'")
      val limit = Some(2)
      val offset = Some(1)

      val response = userController.retrieve(filter, limit, offset)(adminRequest)
      val responseBody = contentAsJson(response)

      (responseBody \ "count").as[Int] mustEqual 2
      (responseBody \ "offset").as[Int] mustEqual 1
      (responseBody \ "results").as[Seq[JsObject]].foreach { user =>
        (user \ "lastName").as[String] must be > "Doppler"
      }
    }

    "throw ParseException on invalid filter" in {
      val invalidFilter = Some("some invalid non-parseable filter")

      intercept[ParseException] {
        await(userController.retrieve(invalidFilter, None, None)(adminRequest))
      }
    }
  }

  "update" should {
    "update user" in {
      val requestBody =
        UpdateUserDto(mikkel.email, mikkel.firstName, mikkel.lastName)
      val request = adminRequest.withBody(requestBody)

      val response = userController.update(mikkel.id)(request)

      status(response) mustEqual NO_CONTENT
      val result = execute(userDao.users.filter(_.id === mikkel.id))
      result must contain only mikkel
    }
  }

  "updateRoles" should {
    "allow a manager promote to manager" in {
      val request =
        FakeRequest()
          .withHeaders(hannahAuthHeader)
          .withBody(UserRoleDto(Set(Manager), Set.empty))

      val response = userController.updateRoles(mikkel.id)(request)

      status(response) mustEqual NO_CONTENT

      val result = execute(userDao.users.filter(_.id === mikkel.id))
      result.head.roles must contain only Manager
    }
    "deny a manager promote to admin" in {
      val request =
        FakeRequest()
          .withHeaders(hannahAuthHeader)
          .withBody(UserRoleDto(Set(Admin), Set.empty))

      val response = userController.updateRoles(mikkel.id)(request)

      status(response) mustEqual FORBIDDEN

      val result = execute(userDao.users.filter(_.id === mikkel.id))
      result.head.roles must not contain Admin
    }
  }

  "updatePassword" should {
    "change a user's password" in {
      val request =
        FakeRequest()
          .withHeaders(hannahAuthHeader)
          .withBody(ChangePasswordDto("myNewSecretPwd"))

      val response = userController.updatePassword()(request)

      status(response) mustEqual NO_CONTENT

      val newRequest =
        FakeRequest()
          .withHeaders(buildBasicAuthHeader(hannah.email, "myNewSecretPwd"))

      val newResponse =
        userController
          .retrieve(Some("firstName eq 'Hannah'"), None, None)(newRequest)

      status(newResponse) mustEqual OK
    }
  }

  "delete" should {
    "delete user" in {
      await(userController.delete(3)(adminRequest))

      val result = execute(userDao.users.filter(_.id === 3L))
      result mustBe empty
    }
  }
}

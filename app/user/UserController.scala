package user

import user.auth.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import domain.{Page, User}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.ExecutionContext
import util.filter.FilterExpression.stringEq
import util.filter.FilterOptions

@Singleton
class UserController @Inject()(
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    userService: UserService)(
    implicit ec: ExecutionContext) extends BaseController {

  private implicit val userFormat = Json.format[User]
  private implicit val userWithPasswordFormat = Json.format[UserWithPassword]
  private implicit val pageWrites = Json.format[Page[User]]

  def create() = {
    silhouette.UnsecuredAction.async(parse.json[UserWithPassword]) { request =>
      userService
        .create(request.body.user, request.body.password)
        .map(u => Created(Json.toJson(u)))
    }
  }

  def retrieve(
      firstName: Option[String],
      lastName: Option[String],
      email: Option[String],
      limit: Option[Int],
      offset: Option[Int]) = silhouette.SecuredAction.async {
    val filter =
      stringEq(UserField.FirstName, firstName)
        .and(stringEq(UserField.LastName, lastName))
        .and(stringEq(UserField.Email, email))

    userService
      .retrieve(FilterOptions(filter, limit, offset))
      .map(u => Ok(Json.toJson(u)))
  }

  def update() = silhouette.SecuredAction.async(parse.json[User]) { request =>
    userService.update(request.body).map(_ => Ok)
  }

  def delete(id: Long) = silhouette.SecuredAction.async {
    userService.delete(id).map(_ => Ok)
  }
}

case class UserWithPassword(user: User, password: String)

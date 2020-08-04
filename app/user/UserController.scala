package user

import com.mohiva.play.silhouette.api.Silhouette
import domain.UserRole.{Admin, Manager}
import domain.{Page, User, UserRole}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.ExecutionContext
import auth.{AuthEnv, Is, OnlyModifies}
import filter.FilterExpression.stringEq
import filter.FilterOptions

@Singleton
class UserController @Inject()(
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    userService: UserService)(
    implicit ec: ExecutionContext) extends BaseController {

  private implicit val userWrites = Json.writes[User]
  private implicit val userDtoReads = Json.reads[UserDto]
  private implicit val pageWrites = Json.writes[Page[User]]
  implicit val userRoleReads = Reads.enumNameReads(UserRole)
  private implicit val userRoleDtoReads = Json.reads[UserRoleDto]

  private val isManagerOrAdmin =
    silhouette.SecuredAction(Is(Manager) || Is(Admin))

  private val canPromoteUser =
    silhouette.SecuredAction(Is(Manager) && OnlyModifies(Manager) || Is(Admin))

  def create() = Action.async(parse.json[UserDto]) { request =>
    userService
      .create(request.body.user, request.body.password)
      .map(u => Created(Json.toJson(u)))
  }

  def retrieve(
      firstName: Option[String],
      lastName: Option[String],
      email: Option[String],
      limit: Option[Int],
      offset: Option[Int]) = isManagerOrAdmin.async {
        val filter =
          stringEq(UserField.FirstName, firstName)
            .and(stringEq(UserField.LastName, lastName))
            .and(stringEq(UserField.Email, email))

        userService
          .retrieve(FilterOptions(filter, limit, offset))
          .map(u => Ok(Json.toJson(u)))
      }

  def update(id: Long) = {
    isManagerOrAdmin.async(parse.json[UserDto]) { request =>
      userService.update(request.body.user.copy(id = id)).map(_ => NoContent)
    }
  }

  def updateRoles(id: Long) = {
    canPromoteUser.async(parse.json[UserRoleDto]) { request =>
      userService
        .updateRoles(id, request.body.add, request.body.remove)
        .map(_ => NoContent)
    }
  }

  def delete(id: Long) = isManagerOrAdmin.async {
    userService.delete(id).map(_ => NoContent)
  }
}

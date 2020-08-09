package user

import auth.{AuthEnv, Is, OnlyModifies}
import com.mohiva.play.silhouette.api.Silhouette
import domain.UserRole.{Admin, Manager}
import domain.{Page, User, UserRole}
import filter.FilterOptions
import javax.inject.{Inject, Singleton}
import parser.UserFilterQueryParser
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    userService: UserService,
    filterQueryParser: UserFilterQueryParser)(
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

  def retrieve(filter: String, limit: Option[Int], offset: Option[Int]) = {
    isManagerOrAdmin.async {
      for {
        filterExpression <- Future.fromTry(filterQueryParser.parse(filter))
        filterOptions = FilterOptions(filterExpression, limit, offset)
        result <- userService.retrieve(filterOptions)
      } yield Ok(Json.toJson(result))
    }
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

package record

import auth.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import domain.UserRole.Admin
import domain._
import java.time.Duration
import javax.inject.Inject
import play.api.libs.json._
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.ExecutionContext

class RecordController @Inject()(
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    recordService: RecordService)(
    implicit ec: ExecutionContext) extends BaseController {

  implicit val distanceFormat = Json.format[Distance]
  implicit val durationReads = Reads {
    case JsNumber(value) => JsSuccess(Duration.ofSeconds(value.toLongExact))
    case other => JsError(s"Incorrect duration $other")
  }
  implicit val locationFormat = Json.format[Location]
  implicit val recordDtoReads = Json.reads[RecordDto]
  implicit val weatherConditionsWrites = Json.writes[WeatherConditions]
  implicit val recordWrites = Json.writes[Record]
  implicit val pageWrites = Json.writes[Page[Record]]

  def create() = {
    silhouette.SecuredAction.async(parse.json[RecordDto]) { request =>
      val ownerId = getRecordOwnerId(request)
      val newRecord = request.body.record.copy(userId = ownerId)
      recordService.create(newRecord).map(r => Created(Json.toJson(r)))
    }
  }

  def retrieveAll() = silhouette.SecuredAction.async { request =>
    val loggedUser = request.identity
    val maybeUserId = Option.unless(loggedUser.is(Admin))(loggedUser.id)
    recordService.retrieveAll(maybeUserId).map(r => Ok(Json.toJson(r)))
  }

  def update(recordId: Long) = {
    silhouette.SecuredAction.async(parse.json[RecordDto]) { request =>
      val ownerId = getRecordOwnerId(request)
      val record = request.body.record
      val updatedRecord = record.copy(id = recordId, userId = ownerId)
      recordService.update(updatedRecord).map(_ => NoContent)
    }
  }

  def delete(recordId: Long) = silhouette.SecuredAction.async { request =>
    val loggedUser = request.identity
    val result =
      if (loggedUser.is(Admin)) recordService.delete(recordId)
      else recordService.delete(loggedUser.id, recordId)

    result.map(_ => NoContent)
  }

  private def getRecordOwnerId(request: SecuredRequest[AuthEnv, RecordDto]) = {
    val loggedUser = request.identity
    if (loggedUser.is(Admin)) {
      request.body.maybeUserId.getOrElse(loggedUser.id)
    } else {
      loggedUser.id
    }
  }
}

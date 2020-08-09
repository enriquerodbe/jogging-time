package record

import auth.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import domain.UserRole.Admin
import domain._
import filter.FilterOptions
import java.time.Duration
import javax.inject.Inject
import parser.RecordFilterQueryParser
import play.api.libs.json._
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}

class RecordController @Inject()(
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    recordService: RecordService,
    filterQueryParser: RecordFilterQueryParser)(
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

  def retrieve(filter: String, limit: Option[Int], offset: Option[Int]) = {
    silhouette.SecuredAction.async { request =>
      val loggedUser = request.identity
      val maybeUserId = Option.unless(loggedUser.is(Admin))(loggedUser.id)
      for {
        filterExpression <- Future.fromTry(filterQueryParser.parse(filter))
        filterOptions = FilterOptions(filterExpression, limit, offset)
        result <- recordService.retrieve(maybeUserId, filterOptions)
      } yield Ok(Json.toJson(result))
    }
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

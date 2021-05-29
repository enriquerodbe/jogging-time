package record

import auth.AuthEnv
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import domain.UserRole.Admin
import domain._
import filter.FilterOptions
import java.time.Duration
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RecordController @Inject() (
    val controllerComponents: ControllerComponents,
    silhouette: Silhouette[AuthEnv],
    recordService: RecordService,
    filterQueryParser: RecordFilterQueryParser,
    weekReportFilterQueryParser: WeekReportFilterQueryParser,
)(implicit ec: ExecutionContext)
    extends BaseController {

  implicit val distanceReads = Reads {
    case JsNumber(value) => JsSuccess(Distance(value.intValue))
    case other => JsError(s"Incorrect distance $other")
  }

  implicit val durationReads = Reads {
    case JsString(value) => JsSuccess(Duration.parse(value))
    case other => JsError(s"Incorrect duration $other")
  }

  implicit val distanceWrites = Writes[Distance](d => JsNumber(d.value))
  implicit val locationFormat = Json.format[Location]
  implicit val recordDtoReads = Json.reads[RecordDto]
  implicit val weatherConditionsWrites = Json.writes[WeatherConditions]
  implicit val recordWrites = Json.writes[Record]
  implicit val pageWrites = Json.writes[Page[Record]]
  implicit val speedWrites = Writes[Speed](speed => JsNumber(speed.value))
  implicit val weekReportWrites = Json.writes[WeekReport]
  implicit val weekReportPageWrites = Json.writes[Page[WeekReport]]

  def create() =
    silhouette.SecuredAction.async(parse.json[RecordDto]) { request =>
      val ownerId = getRecordOwnerId(request)
      val newRecord = request.body.record.copy(userId = ownerId)
      recordService.create(newRecord).map(r => Created(Json.toJson(r)))
    }

  def retrieve(filter: Option[String], limit: Option[Int], offset: Option[Int]) =
    silhouette.SecuredAction.async { request =>
      val loggedUser = request.identity
      val maybeUserId = Option.unless(loggedUser.is(Admin))(loggedUser.id)
      for {
        filterExpression <- Future.fromTry(filterQueryParser.parse(filter))
        filterOptions = FilterOptions(filterExpression, limit, offset)
        result <- recordService.retrieve(maybeUserId, filterOptions)
      } yield Ok(Json.toJson(result))
    }

  def retrieveReport(
      userId: Option[Long],
      filter: Option[String],
      limit: Option[Int],
      offset: Option[Int],
  ) = silhouette.SecuredAction.async { request =>
    val loggedUser = request.identity
    val maybeUserId = if (loggedUser.is(Admin)) userId else Some(loggedUser.id)
    for {
      filterExpression <- Future.fromTry(weekReportFilterQueryParser.parse(filter))
      filterOptions = FilterOptions(filterExpression, limit, offset)
      result <- recordService.retrieveReport(maybeUserId, filterOptions)
    } yield Ok(Json.toJson(result))
  }

  def update(recordId: Long) =
    silhouette.SecuredAction.async(parse.json[RecordDto]) { request =>
      val ownerId = getRecordOwnerId(request)
      val record = request.body.record
      val updatedRecord = record.copy(id = recordId, userId = ownerId)
      recordService.update(updatedRecord).map(_ => NoContent)
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
    val maybeUserId = if (loggedUser.is(Admin)) request.body.userId else None
    maybeUserId.getOrElse(loggedUser.id)
  }

}

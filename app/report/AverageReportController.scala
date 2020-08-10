package report

import auth.{AuthEnv, Is}
import com.mohiva.play.silhouette.api.Silhouette
import domain.{AverageReport, Distance, Page, Speed}
import domain.UserRole.Admin
import filter.FilterOptions
import javax.inject.Inject
import parser.AverageReportFilterQueryParser
import play.api.libs.json.{JsNumber, Json, Writes}
import play.api.mvc.{BaseController, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}

class AverageReportController @Inject()(
    val controllerComponents: ControllerComponents,
    averageReportsService: AverageReportService,
    filterQueryParser: AverageReportFilterQueryParser,
    silhouette: Silhouette[AuthEnv])(
    implicit ec: ExecutionContext) extends BaseController {

  implicit val speedWrites =
    Writes[Speed](speed => JsNumber(speed.value))
  implicit val distanceWrites =
    Writes[Distance](distance => JsNumber(distance.value))
  implicit val averageReportWrites = Json.writes[AverageReport]
  private implicit val pageWrites = Json.writes[Page[AverageReport]]

  def create(days: Int) = silhouette.SecuredAction(Is(Admin)).async {
    averageReportsService
      .create(days)
      .map(reports => Ok(Json.toJson(reports)))
  }

  def retrieve(
      filter: String,
      maybeUserId: Option[Long],
      limit: Option[Int],
      offset: Option[Int]) = silhouette.SecuredAction.async { request =>
    val loggedUser = request.identity
    val userId = if (loggedUser.is(Admin)) maybeUserId else Some(loggedUser.id)
    for {
      filterExpression <- Future.fromTry(filterQueryParser.parse(filter))
      filterOptions = FilterOptions(filterExpression, limit, offset)
      result <- averageReportsService.retrieve(userId, filterOptions)
    } yield Ok(Json.toJson(result))
  }
}

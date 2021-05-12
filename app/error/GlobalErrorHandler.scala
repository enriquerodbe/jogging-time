package error

import javax.inject.Inject
import filter.parser.ParseException
import play.api.http.JsonHttpErrorHandler
import play.api.http.Status.BAD_REQUEST
import play.api.mvc.{RequestHeader, Result}
import play.api.{Environment, OptionalSourceMapper}
import scala.concurrent.Future

class GlobalErrorHandler @Inject() (
    environment: Environment,
    optionalSourceMapper: OptionalSourceMapper,
) extends JsonHttpErrorHandler(environment, optionalSourceMapper) {

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    exception match {
      case pe: ParseException => onClientError(request, BAD_REQUEST, pe.msg)
      case throwable => super.onServerError(request, throwable)
    }

}

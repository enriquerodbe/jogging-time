package weather

import com.typesafe.config.Config
import domain.{Location, WeatherConditions}
import java.time.Instant
import javax.inject.Inject
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.DurationConverters.JavaDurationOps

class OpenWeatherMapClient @Inject()(
    ws: WSClient,
    config: Config)(
    implicit ec: ExecutionContext) extends WeatherService {

  val baseUrl = "http://api.openweathermap.org/data/2.5/onecall/timemachine"
  val openWeatherMapAppId = config.getString("openWeatherMap.appid")
  val openWeatherMapTimeout = config.getDuration("openWeatherMap.timeout")

  private implicit val weatherConditionsReads: Reads[WeatherConditions] = (
    (__ \ "temp").read[BigDecimal] and
    (__ \ "humidity").read[Int] and
    (__ \ "wind_speed").read[BigDecimal]
  )(WeatherConditions.apply _)

  override def retrieve(
      location: Location,
      date: Instant): Future[WeatherConditions] = {
    ws.url(baseUrl)
      .withQueryStringParameters(
        "lat" -> location.lat.toString,
        "lon" -> location.lon.toString,
        "dt" -> date.getEpochSecond.toString,
        "units" -> "imperial",
        "appid" -> openWeatherMapAppId)
      .withRequestTimeout(openWeatherMapTimeout.toScala)
      .get()
      .map(_.json \ "current")
      .map(_.validate[WeatherConditions].get)
  }
}

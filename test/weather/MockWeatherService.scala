package weather
import domain.{Location, WeatherConditions}
import java.time.Instant
import scala.concurrent.Future

class MockWeatherService extends WeatherService {

  override def retrieve(
      location: Location,
      date: Instant): Future[WeatherConditions] = {
    Future.successful(WeatherConditions(21.2, 66, 10))
  }
}

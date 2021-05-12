package weather

import domain.{Location, WeatherConditions}
import fixture.Fixture.today
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class MockWeatherService extends WeatherService {

  override def retrieve(location: Location, date: Instant): Future[WeatherConditions] = {
    if (date.isBefore(today.minus(5, ChronoUnit.DAYS))) {
      val msg = "Requested time is out of allowed range of 5 days back"
      Future.failed(new Exception(msg))
    } else {
      Future.successful(WeatherConditions(21.2, 66, 10))
    }
  }

}

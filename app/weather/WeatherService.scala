package weather

import com.google.inject.ImplementedBy
import domain.{Location, WeatherConditions}
import java.time.Instant
import scala.concurrent.Future

@ImplementedBy(classOf[OpenWeatherMapClient])
trait WeatherService {

  def retrieve(location: Location, date: Instant): Future[WeatherConditions]
}

package domain

case class WeatherConditions(
    temperature: BigDecimal,
    humidity: Int,
    windSpeed: BigDecimal,
)

object WeatherConditions {

  def fromValues(
      maybeTemperature: Option[BigDecimal],
      maybeHumidity: Option[Int],
      maybeWindSpeed: Option[BigDecimal],
  ): Option[WeatherConditions] = {
    (maybeTemperature, maybeHumidity, maybeWindSpeed) match {
      case (Some(temperature), Some(humidity), Some(windSpeed)) =>
        Some(WeatherConditions(temperature, humidity, windSpeed))

      case _ => None
    }
  }

  def toTuple(
      weather: Option[WeatherConditions]
  ): Option[(Option[BigDecimal], Option[Int], Option[BigDecimal])] = {
    Some(
      (
        weather.map(_.temperature),
        weather.map(_.humidity),
        weather.map(_.windSpeed),
      )
    )
  }

}

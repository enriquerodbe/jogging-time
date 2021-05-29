package domain

case class WeatherConditions(
    temperature: BigDecimal,
    humidity: Int,
    windSpeed: BigDecimal,
)

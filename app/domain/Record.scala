package domain

import java.time.{Duration, Instant}

case class Record(
    id: Long,
    userId: Long,
    date: Instant,
    distance: Distance,
    duration: Duration,
    location: Location,
    weather: Option[WeatherConditions]
)

package domain

import java.time.Instant

case class Record(
    id: Long,
    userId: Long,
    date: Instant,
    distance: Distance,
    duration: java.time.Duration,
    location: Location,
    weather: Option[WeatherConditions],
)

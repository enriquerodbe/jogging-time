package record

import domain.{Distance, Location, Record}
import java.time.{Duration, Instant}

case class RecordDto(
    userId: Option[Long],
    date: Instant,
    distance: Distance,
    duration: Duration,
    location: Location,
) {

  def record: Record = {
    Record(
      -1,
      userId.getOrElse(-1),
      date,
      distance,
      duration,
      location,
      None,
    )
  }

}

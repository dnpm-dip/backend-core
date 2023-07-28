package de.dnpm.dip.model


import java.time.Instant
import play.api.libs.json.{
  Json,
  Format,
  Reads,
  Writes
}


final case class Snapshot[T]
(
  id: Id[Snapshot[T]],
  data: T,
  timestamp: Long = Instant.now.toEpochMilli
)

object Snapshot
{

  implicit def format[T: Format] =
    Json.format[Snapshot[T]]

}

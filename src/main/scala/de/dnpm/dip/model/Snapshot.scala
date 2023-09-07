package de.dnpm.dip.model


import java.time.Instant
import play.api.libs.json.{
  Json,
  Format,
  Reads,
  Writes
}


final case class Snapshot[T] private (
//  id: Id[Snapshot[T]],
  data: T,
  id: Long// = Instant.now.toEpochMilli
//  timestamp: Long = Instant.now.toEpochMilli
)

object Snapshot
{

  def apply[T](data: T): Snapshot[T] =
    Snapshot(data,Instant.now.toEpochMilli)


  implicit def format[T: Format] =
    Json.format[Snapshot[T]]

}

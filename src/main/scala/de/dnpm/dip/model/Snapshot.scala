package de.dnpm.dip.model


import java.time.Instant
import play.api.libs.json.{
  Json,
  Reads,
  Writes,
  OWrites,
}


final case class Snapshot[T] private (
  data: T,
  timestamp: Long // epoch milliseconds
)

object Snapshot
{

  def of[T](data: T): Snapshot[T] =
    Snapshot(data,Instant.now.toEpochMilli)


  implicit def reads[T: Reads]: Reads[Snapshot[T]] =
    Json.reads[Snapshot[T]]

  implicit def writes[T: Writes]: OWrites[Snapshot[T]] =
    Json.writes[Snapshot[T]]

}

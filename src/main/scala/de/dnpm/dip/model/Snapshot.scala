package de.dnpm.dip.model


import java.time.Instant
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}


final case class Snapshot[T] private (
  data: T,
  timestamp: Long // epoch milliseconds
)

object Snapshot
{

  def apply[T](data: T): Snapshot[T] =
    Snapshot(data,Instant.now.toEpochMilli)


  implicit def format[T: Format]: OFormat[Snapshot[T]] =
    Json.format[Snapshot[T]]

}

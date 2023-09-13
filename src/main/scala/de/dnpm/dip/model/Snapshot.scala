package de.dnpm.dip.model


import java.time.Instant
import play.api.libs.json.{
  Json,
  Format
}


final case class Snapshot[T] private (
  data: T,
  id: Long
)

object Snapshot
{

  def apply[T](data: T): Snapshot[T] =
    Snapshot(data,Instant.now.toEpochMilli)


  implicit def format[T: Format]: Format[Snapshot[T]] =
    Json.format[Snapshot[T]]

}

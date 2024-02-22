package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.Temporal
import play.api.libs.json.{
  Json,
  Reads,
  Writes,
  OWrites
}


final case class History[T]
(
  history: List[T]
)
{
  import scala.language.reflectiveCalls


  def latestBy[U <: Temporal: Ordering](f: T => U) =
    history.maxByOption(f)

  def latest(
    implicit hasRecDate: T <:< { def recordedOn: LocalDate }
  ): Option[T] =
    latestBy(_.recordedOn)

}

object History
{

  implicit def reads[T: Reads]: Reads[History[T]] =
    Json.reads[History[T]]

  implicit def writes[T: Writes]: OWrites[History[T]] =
    Json.writes[History[T]]
}


/*
final case class History[T]
(
  history: List[T]
)
{
  import scala.language.reflectiveCalls

  def latest(
    implicit hasRecDate: T <:< { def recordedOn: LocalDate }
  ): Option[T] =
    history
      .maxByOption(_.recordedOn)
}

object History
{

  implicit def reads[T: Reads]: Reads[History[T]] =
    Json.reads[History[T]]

  implicit def writes[T: Writes]: OWrites[History[T]] =
    Json.writes[History[T]]
}
*/

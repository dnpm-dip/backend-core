package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.Temporal
import cats.data.NonEmptyList
import play.api.libs.json.{
  Json,
  Reads,
  Writes,
  OWrites
}
import de.dnpm.dip.util.Completer


final case class History[+T]
(
  history: NonEmptyList[T]
)
{
  import scala.language.reflectiveCalls
  import cats.Order
  

  def latestBy[U <: Temporal: Ordering](f: T => U) =
    history.toList.maxBy(f)

  def latest(
    implicit hasRecDate: T <:< { def recordedOn: LocalDate }
  ): T =
    latestBy(_.recordedOn)


  def orderedBy[U <: Temporal: Ordering](f: T => U) =
    copy(
      history = history.sortBy(f)(Order.reverse(Order.fromOrdering[U]))
    )

  def ordered(
    implicit hasRecDate: T <:< { def recordedOn: LocalDate }
  ) =
    orderedBy(_.recordedOn)

}

object History
{
  import de.dnpm.dip.util.json.{ 
    readsNel,
    writesNel
  }

  def apply[T](t: T, ts: T*): History[T] =
    History(NonEmptyList.one(t) ++ ts.toList)

/*
  import scala.language.reflectiveCalls

  implicit def readsHasIdHistory[T <: { def id: Id[_] }: Reads]: Reads[History[T]] =
    Json.reads[History[T]]
      .filter(
        JsonValidationError("A History pertains to one Entity, all entries must have the same ID")
      )(
        h => h.history.forall(_.id == h.history.head.id)
      )
*/


  import Completer.syntax._

  implicit def historyCompleter[T: Completer]: Completer[History[T]] =
    Completer.of(
      h => h.copy(
        history = h.history.complete
      )
    )

  implicit def reads[T: Reads]: Reads[History[T]] =
    Json.reads[History[T]]

  implicit def writes[T: Writes]: OWrites[History[T]] =
    Json.writes[History[T]]
}


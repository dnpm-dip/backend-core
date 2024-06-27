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
  import de.dnpm.dip.util.json._

  def apply[T](t: T, ts: T*): History[T] =
    History(NonEmptyList.one(t) ++ ts.toList)


  implicit def reads[T: Reads]: Reads[History[T]] =
    Json.reads[History[T]]

  implicit def writes[T: Writes]: OWrites[History[T]] =
    Json.writes[History[T]]
}

//  history: List[T]
/*    
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
*/

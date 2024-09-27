package de.dnpm.dip.model


import java.time.temporal.{
  ChronoUnit,
  Temporal
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat,
  Reads,
  Writes,
  OWrites
}



sealed abstract class Period[T <: Temporal]
{
  val start: T

  def endOption: Option[T] =
    this match {
      case p: ClosedPeriod[T]  => Some(p.end)
      case p: OpenEndPeriod[T] => p.end
    }

  def contains(t: T)(implicit order: Ordering[T]): Boolean


  def duration(u: UnitOfTime): Option[Duration] =
    this.endOption.map(
      end =>
      Duration(
        UnitOfTime
          .chronoUnit(u)
          .between(start,end)
          .toDouble,
        u
      )
    )

  def duration(u: ChronoUnit): Option[Duration] =
    this.duration(UnitOfTime.of(u))

}


final case class ClosedPeriod[T <: Temporal]
(
  start: T,
  end: T
)
extends Period[T]
{
  override def contains(t: T)(implicit order: Ordering[T]) =
    order.lteq(start,t) && order.gteq(end,t)
}

final case class OpenEndPeriod[T <: Temporal]
(
  start: T,
  end: Option[T]
)
extends Period[T]
{
  override def contains(t: T)(implicit order: Ordering[T]) =
    order.lteq(start,t) && end.map(order.gteq(_,t)).getOrElse(true)

  def withEnd(end: T): ClosedPeriod[T] =
    ClosedPeriod(start,end)

}


object OpenEndPeriod
{
  implicit def format[T <: Temporal: Format]: OFormat[OpenEndPeriod[T]] =
    Json.format[OpenEndPeriod[T]]
}


object ClosedPeriod
{
  implicit def format[T <: Temporal: Format]: OFormat[ClosedPeriod[T]] =
    Json.format[ClosedPeriod[T]]
}


object Period
{

  def apply[T <: Temporal](start: T, end: Option[T] = None): Period[T] =
    OpenEndPeriod(start,end)

  def apply[T <: Temporal](start: T, end: T): Period[T] =
    ClosedPeriod(start,end)


  implicit def format[T <: Temporal: Format]: OFormat[Period[T]] =
    OFormat[Period[T]](
      Reads { 
        js =>
          js.validate[ClosedPeriod[T]]
            .orElse(
              js.validate[OpenEndPeriod[T]]
            )
      },
      OWrites {
        case p: OpenEndPeriod[T] => Json.toJsObject(p)
        case p: ClosedPeriod[T]  => Json.toJsObject(p)
      }
    )

}


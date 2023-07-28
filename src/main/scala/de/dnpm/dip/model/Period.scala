package de.dnpm.dip.model


import java.time.temporal.Temporal


sealed abstract class Period[T <: Temporal]
{
  val start: T

  def contains(t: T)(implicit order: Ordering[T]): Boolean
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
}


object OpenEndPeriod
{

  def apply[T <: Temporal](start: T): OpenEndPeriod[T] = OpenEndPeriod(start,None)
}



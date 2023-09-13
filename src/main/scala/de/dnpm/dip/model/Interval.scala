package de.dnpm.dip.model



import play.api.libs.json.{
  Format,Json,Reads,Writes,JsObject,JsSuccess
}


sealed abstract class Interval[T: Ordering]
{
  def contains(t: T): Boolean
  def ∋ (t: T) = contains(t)
  override def toString: String
}

import Interval._


case class OpenInterval[T: Ordering](min: T, max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min < t && t < max)
  override def toString = s"($min,$max)"
}
object OpenInterval
{
  def apply[T: Ordering](minMax: (T,T)): OpenInterval[T] =
    OpenInterval(minMax._1,minMax._2)

  implicit def format[T: Ordering: Format]: Format[OpenInterval[T]] =
    Json.format[OpenInterval[T]]
}


case class ClosedInterval[T: Ordering](min: T, max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min <= t && t <= max)
  override def toString = s"[$min,$max]"
}
object ClosedInterval
{

  def apply[T: Ordering](minMax: (T,T)): ClosedInterval[T] =
    ClosedInterval(minMax._1,minMax._2)

  implicit def format[T: Ordering: Format]: Format[ClosedInterval[T]] =
    Json.format[ClosedInterval[T]]
}


case class LeftOpenRightClosedInterval[T: Ordering](min: T, max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min < t && t <= max)
  override def toString = s"($min,$max]"
}
object LeftOpenRightClosedInterval
{
  def apply[T: Ordering](minMax: (T,T)): LeftOpenRightClosedInterval[T] =
    LeftOpenRightClosedInterval(minMax._1,minMax._2)

  implicit def format[T: Ordering: Format]: Format[LeftOpenRightClosedInterval[T]] =
    Json.format[LeftOpenRightClosedInterval[T]]
}


case class LeftClosedRightOpenInterval[T: Ordering](min: T, max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min <= t && t < max)
  override def toString = s"[$min,$max)"
}
object LeftClosedRightOpenInterval
{
  def apply[T: Ordering](minMax: (T,T)): LeftClosedRightOpenInterval[T] =
    LeftClosedRightOpenInterval(minMax._1,minMax._2)

  implicit def format[T: Ordering: Format]: Format[LeftClosedRightOpenInterval[T]] =
    Json.format[LeftClosedRightOpenInterval[T]]
}


case class LeftOpenInterval[T: Ordering](min: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min < t)
  override def toString = s"($min,∞)"
}
object LeftOpenInterval
{
  implicit def format[T: Ordering: Format]: Format[LeftOpenInterval[T]] =
    Json.format[LeftOpenInterval[T]]
}


case class LeftClosedInterval[T: Ordering](min: T) extends Interval[T]
{
  def contains(t: T): Boolean = (min <= t)
  override def toString = s"[$min,∞)"
}
object LeftClosedInterval
{
  implicit def format[T: Ordering: Format]: Format[LeftClosedInterval[T]] =
    Json.format[LeftClosedInterval[T]]
}


case class RightOpenInterval[T: Ordering](max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (t < max)
  override def toString = s"(-∞,$max)"
}
object RightOpenInterval
{
  implicit def format[T: Ordering: Format]: Format[RightOpenInterval[T]] =
    Json.format[RightOpenInterval[T]]
}


case class RightClosedInterval[T: Ordering](max: T) extends Interval[T]
{
  def contains(t: T): Boolean = (t <= max)
  override def toString = s"(-∞,$max]"
}
object RightClosedInterval
{
  implicit def format[T: Ordering: Format]: Format[RightClosedInterval[T]] =
    Json.format[RightClosedInterval[T]]
}


case class UnboundedInterval[T: Ordering]() extends Interval[T]
{
  def contains(t: T): Boolean = true
  override def toString = s"(-∞,∞)"
}
object UnboundedInterval
{
  implicit def format[T: Ordering: Format]: Format[UnboundedInterval[T]] =
    Format[UnboundedInterval[T]](
      Reads(js => JsSuccess(UnboundedInterval[T]())),
      Writes(_ => JsObject.empty)
    )
}




object Interval
{

  implicit class OrderOps[T](val t: T) extends AnyVal
  {
    def <(u: T)(implicit o: Ordering[T])  = o.lt(t,u)
    def <=(u: T)(implicit o: Ordering[T]) = o.lteq(t,u)
    def >(u: T)(implicit o: Ordering[T])  = o.gt(t,u)
    def >=(u: T)(implicit o: Ordering[T]) = o.gteq(t,u)
  }


  implicit class IntervalOps[T](val t: T) extends AnyVal
  {
    def isIn(interval: Interval[T]) = interval contains t
    def ∈ (interval: Interval[T]) = t isIn interval
  }


  implicit def format[T: Ordering: Format]: Format[Interval[T]] =
    Format[Interval[T]](
      Reads(
        js =>
          for {
            optMin <- (js \ "min").validateOpt[T]
            optMax <- (js \ "max").validateOpt[T]
          } yield {
            (optMin,optMax) match {
              case (Some(min),Some(max)) => ClosedInterval(min -> max)
              case (Some(min),None)      => LeftClosedInterval(min)
              case (None,Some(max))      => RightClosedInterval(max)
              case (None,None)           => UnboundedInterval()
            } 
          }
      ),
      Writes {
        case rng: OpenInterval[T]                => Json.toJson(rng)
        case rng: ClosedInterval[T]              => Json.toJson(rng)
        case rng: LeftClosedRightOpenInterval[T] => Json.toJson(rng)
        case rng: LeftOpenRightClosedInterval[T] => Json.toJson(rng)
        case rng: LeftOpenInterval[T]            => Json.toJson(rng)
        case rng: LeftClosedInterval[T]          => Json.toJson(rng)
        case rng: RightOpenInterval[T]           => Json.toJson(rng)
        case rng: RightClosedInterval[T]         => Json.toJson(rng)
        case rng: UnboundedInterval[T]           => Json.toJson(rng)
      }
    )


}


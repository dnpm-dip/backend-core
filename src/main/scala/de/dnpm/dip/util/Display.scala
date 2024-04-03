package de.dnpm.dip.util


import play.api.libs.json.{
  Json,
  Format
}


// Utility to map (complex) types to a string label for display purposes

final case class DisplayLabel[T](value: String) extends AnyVal
{
  override def toString = value
}

object DisplayLabel
{

  def of[T](t: T)(implicit displays: Displays[T]): DisplayLabel[T] =
    displays(t)


  implicit def format[T]: Format[DisplayLabel[T]] =
    Json.valueFormat[DisplayLabel[T]]


  implicit def ordering[T]: Ordering[DisplayLabel[T]] =
    Ordering[String].on[DisplayLabel[T]](_.value)
}



trait Displays[T] extends (T => DisplayLabel[T])

object Displays
{

  def apply[T](f: T => String): Displays[T] =
    new Displays[T]{ 
      override def apply(t: T) = DisplayLabel(f(t))
    }

  def apply[T](implicit d: Displays[T]): Displays[T] = d

}

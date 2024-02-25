package de.dnpm.dip.util


import play.api.libs.json.{
  Json,
  Format
}


// Utility to map (compley) types to a string label for display purposes


final case class DisplayLabel[T](value: String) extends AnyVal

object DisplayLabel
{

  def of[T](t: T)(implicit displays: Displays[T]): DisplayLabel[T] =
    displays(t)

  implicit def format[T]: Format[DisplayLabel[T]] =
    Json.valueFormat[DisplayLabel[T]]
}



trait Displays[T] extends (T => DisplayLabel[T])

object Displays
{

  def from[T](f: T => String): Displays[T] =
    new Displays[T]{ 
      override def apply(t: T) = DisplayLabel(f(t))
    }

  def apply[T](implicit d: Displays[T]): Displays[T] = d

}

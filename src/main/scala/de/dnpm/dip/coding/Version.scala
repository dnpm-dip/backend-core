package de.dnpm.dip.coding


import java.time.Year
import play.api.libs.json.{
  Json,
  Format
}


final case class Version[S](value: String) extends AnyVal

object Version
{

  val OrderedByYear =
    Ordering.by((v: String) => Year.of(v.toInt))

  val Unordered =
    new Ordering[String]{
      override def compare(x: String, y: String) = 0
    }


  implicit def format[S]: Format[Version[S]] =
    Json.valueFormat[Version[S]]

}

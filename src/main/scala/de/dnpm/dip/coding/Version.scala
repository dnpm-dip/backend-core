package de.dnpm.dip.coding


import java.time.temporal.Temporal
import java.time.{
  LocalDate,
  Year
}
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import play.api.libs.json.{
  Json,
  Format
}


final case class Version[S](value: String) extends AnyVal

object Version
{

  val OrderedByYear =
    Ordering.by((v: String) => Year.of(v.toInt))

  val OrderedByDate =
    Ordering.by((v: String) => LocalDate.parse(v,ISO_LOCAL_DATE))


  val Unordered =
    new Ordering[String]{
      override def compare(x: String, y: String) = 0
    }



  implicit def format[S]: Format[Version[S]] =
    Json.valueFormat[Version[S]]

}

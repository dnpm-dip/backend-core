package de.dnpm.dip.coding


import java.net.URI
import de.dnpm.dip.util.Displays
import play.api.libs.json.{
  Json,
  Format
}


final case class Code[+S](value: String) extends AnyVal
{
  override def toString = value
}

object Code
{

  implicit def enumCodeDisplays[E <: Enumeration](
    implicit cs: CodeSystem[E#Value]
  ): Displays[Code[E#Value]] =
    Displays[Code[E#Value]](
      e =>
        cs.conceptWithCode(e.value)
          .map(_.display)
          .get
    )


  implicit def format[S]: Format[Code[S]] =
    Json.valueFormat[Code[S]]

}

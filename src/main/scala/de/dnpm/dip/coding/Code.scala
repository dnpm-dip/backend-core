package de.dnpm.dip.coding


import java.net.URI
import de.dnpm.dip.util.Displayer
import play.api.libs.json.{
  Json,
  Format
}


final case class Code[+S](value: String) extends AnyVal

object Code
{

  implicit def enumCodeDisplayer[E <: Enumeration](
    implicit cs: CodeSystem[E#Value]
  ): Displayer[Code[E#Value]] =
    Displayer.from(
      e =>
        cs.conceptWithCode(e.value)
          .map(_.display)
          .get
    )


  implicit def format[S]: Format[Code[S]] =
    Json.valueFormat[Code[S]]

}

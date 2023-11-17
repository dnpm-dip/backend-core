package de.dnpm.dip.coding


import java.net.URI
import play.api.libs.json.{
  Json,
  Format
}


final case class Code[+S](value: String) extends AnyVal

object Code
{

  implicit def format[S]: Format[Code[S]] =
    Json.valueFormat[Code[S]]

}

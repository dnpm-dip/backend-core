package de.dnpm.dip.coding


import java.net.URI
import play.api.libs.json.Json


final case class Code[+S](value: String) extends AnyVal

object Code
{

/*    
  import scala.language.implicitConversions

  implicit def toStaticCodingWithDisplay[S: Coding.System](
    code: Code[S]
  )(
    implicit vs: ValueSet[S]
  ): StaticCoding[S] =
    StaticCoding[S](
      code,
      vs.displayOf(code),
      vs.version
    )

  implicit def toBasicCodingWithDisplay[S: Coding.System](
    code: Code[S]
  )(
    implicit vs: ValueSet[S]
  ): BasicCoding =
    BasicCoding(
      code.asInstanceOf[Code[Any]],
      vs.displayOf(code),
      vs.uri,
      vs.version
    )

  implicit def toStaticCoding[S: Coding.System](code: Code[S]): StaticCoding[S] =
    Coding[S](code.value)

  implicit def fromCoding[S: Coding.System](coding: StaticCoding[S]): Code[S] =
    coding.code
*/

  implicit def format[S] = Json.valueFormat[Code[S]]

}



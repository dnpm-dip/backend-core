package de.dnpm.dip.coding

    
import play.api.libs.json.Json


final case class Display[S](value: String) extends AnyVal

object Display
{

  import scala.language.implicitConversions


  def of[E <: Enumeration](
    e: E#Value
  )(
    implicit cs: CodeSystem[E#Value]
  ): Display[E#Value] =
    cs.concept(Code[E#Value](e.toString))
      .map(c => Display[E#Value](c.display))
      .get


  implicit def fromCodeVS[S](
    code: Code[S]
  )(
    implicit vs: ValueSet[S]
  ): Display[S] =
    vs.displayOf(code)
      .map(Display[S](_))
      .get


  implicit def fromCodingVS[S](
    model: Coding[S]
  )(
    implicit vs: ValueSet[S]
  ): Display[S] =
    model.display
      .orElse(
        vs.displayOf(model.code)
      )
      .map(Display[S](_))
      .get


  implicit def format[S] =
    Json.valueFormat[Display[S]]


}

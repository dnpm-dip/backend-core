package de.dnpm.dip.model


import java.net.URI
import play.api.libs.json.{
  Json,
  JsonValidationError,
  JsPath,
  Format,
  Reads,
  OWrites
}
import play.api.libs.functional.syntax._
import shapeless.Coproduct
import de.dnpm.dip.coding.Coding


final case class Id[+T](value: String)
{

  override def toString: String = value


  import scala.language.reflectiveCalls

  def resolveOn[TT >: T](
    ts: Iterable[TT]
  )(
    implicit hasId: TT <:< { def id: Id[TT] }
  ): Option[TT] =
    ts.find(_.id == this)
  
}

object Id
{
  implicit def format[T]: Format[Id[T]] =
    Json.valueFormat[Id[T]]
}


final case class ExternalId[+T,+S]
(
  value: String,
  system: URI
)

object ExternalId
{

  def apply[T, S: Coding.System](
    value: String
  ): ExternalId[T,S] =
    ExternalId(
      value,
      Coding.System[S].uri
    )


  implicit def writes[T,S]: OWrites[ExternalId[T,S]] =
    Json.writes[ExternalId[T,S]]

  implicit def readsAnyExtId[T]: Reads[ExternalId[T,Any]] =
    (
      (JsPath \ "value").read[String] and
      (JsPath \ "system").read[URI]
    )(
      ExternalId[T,Any](_,_)
    )


  implicit def readsExtId[T,S](
    implicit sys: Coding.System[S]
  ): Reads[ExternalId[T,S]] =
    (
      (JsPath \ "value").read[String] and
      (JsPath \ "system").readNullable[URI] 
    )(
      (v,_) => ExternalId[T,S](v)
    )

  implicit def readsSystemUnionExtId[T,S <: Coproduct](
    implicit uris: Coding.System.UriSet[S]
  ): Reads[ExternalId[T,S]] =
    (
      (JsPath \ "value").read[String] and
      (JsPath \ "system").read[URI] 
    )(
      (v,sys) => ExternalId[T,S](v,sys)
    )
    .filter(
      JsonValidationError(s"Invalid 'system' value, expected one of {${uris.values.mkString(", ")}}")
    )(
      extId => uris.values.contains(extId.system)
    )


}


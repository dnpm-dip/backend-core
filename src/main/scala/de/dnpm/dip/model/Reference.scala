package de.dnpm.dip.model


import java.net.URI
import cats.data.Ior
import play.api.libs.json.{
  Json,
  JsObject,
  Format,
  Reads,
  Writes
}


sealed trait Reference[+T]
{
  self =>

  val display: Option[String]  

  def resolve[TT >: T](implicit res: Resolver[TT]): Option[TT] =
    res(self)

//  def resolveF[F,TT >: T](implicit res: ResolverF[F]) = res[TT](self)
}


trait Resolver[T]
{
  def apply(ref: Reference[T]): Option[T]
}

object Resolver
{
  def apply[T](implicit res: Resolver[T]) = res
}

trait ResolverF[F[_],Env]
{
  def apply[T](ref: Reference[T])(implicit env: Env): F[Option[T]]
}


final case class UriReference[+T]
(
  uri: URI,
  display: Option[String]
)
extends Reference[T]


final case class IdReference[+T]
(
  id: Id[T],
  display: Option[String]
)
extends Reference[T]


final case class ExternalReference[+T]
(
  extId: ExternalId[T],
  display: Option[String]
)
extends Reference[T]



object Reference
{

  def apply[T](uri: URI, display: Option[String]): Reference[T] =
    UriReference(uri,display)

  def apply[T](id: Id[T], display: Option[String]): Reference[T] =
    IdReference(id,display)

  def apply[T](extId: ExternalId[T], display: Option[String]): Reference[T] =
    ExternalReference(extId,display)

  def uri[T](uri: String, display: Option[String] = None): Reference[T] =
    UriReference(URI.create(uri),display)

  def id[T](id: String, display: Option[String] = None): Reference[T] =
    IdReference(Id(id),display)

  
  def apply[T <: AnyRef { val id: Id[T]}](t: T): Reference[T] = {
    import scala.language.reflectiveCalls

    // Cast id value to String instead of Id[T] here
    // because Id extends AnyVal and is thus a String at runtime
    Reference.id[T](t.asInstanceOf[{ val id: String }].id)
  }


  implicit def formatUriRef[T]          = Json.format[UriReference[T]]
  implicit def formatIdRef[T]           = Json.format[IdReference[T]]
  implicit def formatExternalRef[T]     = Json.format[ExternalReference[T]]


  implicit def formatReference[T]: Format[Reference[T]] =
    Format(
      Reads(js =>
        js.validate[IdReference[T]]
          .orElse(js.validate[ExternalReference[T]]) 
          .orElse(js.validate[UriReference[T]]) 
      ),
      Writes {
        case r: UriReference[T]      => Json.toJson(r)
        case r: IdReference[T]       => Json.toJson(r)
        case r: ExternalReference[T] => Json.toJson(r)
      }
    )

}

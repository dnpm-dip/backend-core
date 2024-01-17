package de.dnpm.dip.model


import java.net.URI
import cats.data.Ior
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  OFormat,
  Reads,
  OWrites
}


sealed trait Reference[+T]
{
  self =>

  val display: Option[String]  

  def resolve[TT >: T](implicit res: Resolver[TT]): Option[TT] =
    res(self)

//  def resolveF[F,TT >: T](implicit res: ResolverF[F]) = res[TT](self)
}


trait Resolver[T] extends (Reference[T] => Option[T])

object Resolver
{
  def apply[T](implicit res: Resolver[T]) = res

  implicit def on[T <: { def id: Id[T] }](implicit ts: Iterable[T]): Resolver[T] =
    new Resolver[T]{
      import scala.language.reflectiveCalls
      override def apply(ref: Reference[T]): Option[T] =
        ref match {
          case IdReference(id,_) => ts.find(_.id == id)
          case _                 => None
        }
    }

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

  type HasId[T] = { def id: Id[T] }


  def apply[T](
    uri: URI,
    display: Option[String]
  ): Reference[T] =
    UriReference(uri,display)

  def apply[T](
    id: Id[T],
    display: Option[String]
  ): Reference[T] =
    IdReference(id,display)

  def apply[T](
    extId: ExternalId[T],
    display: Option[String]
  ): Reference[T] =
    ExternalReference(extId,display)

  def uri[T](
    uri: String,
    display: Option[String] = None
  ): Reference[T] =
    UriReference(URI.create(uri),display)

  def id[T](
    id: String,
    display: Option[String] = None
  ): Reference[T] =
    IdReference(Id(id),display)

  
  def apply[T <: { def id: Id[T] }](t: T): Reference[T] = {
    import scala.language.reflectiveCalls

    Reference.id[T](t.id.value)
  }

/*
  def apply[T <: { val id: Id[T] }](t: T): Reference[T] = {
    import scala.language.reflectiveCalls

    // Cast id value to String instead of Id[T] here
    // because Id extends AnyVal and is thus a String at runtime
    Reference.id[T](t.asInstanceOf[{ val id: String }].id)
  }
*/

  implicit def formatUriRef[T]: OFormat[UriReference[T]] =
    Json.format[UriReference[T]]

  implicit def formatIdRef[T]: OFormat[IdReference[T]] =
    Json.format[IdReference[T]]

  implicit def formatExternalRef[T]: OFormat[ExternalReference[T]] =
    Json.format[ExternalReference[T]]


  final case class TypeName[T](value: String)

  object TypeName
  {
    import scala.reflect.ClassTag

    def apply[T](implicit t: TypeName[T]) = t

    implicit def typeName[T](implicit tag: ClassTag[T]): TypeName[T] =
      TypeName[T](tag.runtimeClass.asInstanceOf[Class[T]].getSimpleName)
  }


  implicit def readsReference[T]: Reads[Reference[T]] =
    Reads(js =>
      js.validate[IdReference[T]]
        .orElse(js.validate[ExternalReference[T]]) 
        .orElse(js.validate[UriReference[T]]) 
    )

  implicit def writesReference[T: TypeName]: OWrites[Reference[T]] =
    OWrites[Reference[T]]{
      case r: UriReference[T]      => Json.toJsObject(r)
      case r: IdReference[T]       => Json.toJsObject(r)
      case r: ExternalReference[T] => Json.toJsObject(r)
    }
    .transform(
      (js: JsObject) => js + ("type" -> JsString(TypeName[T].value))
    )

}

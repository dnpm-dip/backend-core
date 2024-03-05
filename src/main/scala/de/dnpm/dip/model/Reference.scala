package de.dnpm.dip.model


import java.net.URI
import cats.data.Ior
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  JsonValidationError,
  OFormat,
  Reads,
  OWrites
}


trait Resolver[T] extends (Reference[T] => Option[T])

object Resolver
{
  def apply[T](implicit res: Resolver[T]) = res
}


final case class Reference[+T]
(
  id: Option[Id[T]],
  extId: Option[ExternalId[T]],
  uri: Option[URI],
  display: Option[String]
)
{

  def resolve[TT >: T](implicit res: Resolver[TT]): Option[TT] =
    res(this)

  def resolveOn[TT >: T](
    ts: Iterable[TT]
  )(
    implicit hasId: TT <:< { def id: Id[TT] }
  ): Option[TT] = {
    import scala.language.reflectiveCalls
    this.id match {
      case Some(id) => ts.find(_.id == id)
      case _        => None
    }
  }

  def withDisplay(d: String): Reference[T] =
    this.copy(display = Some(d))
}


object Reference
{

  def from[T](
    uri: URI,
  ): Reference[T] =
    Reference(None,None,Some(uri),None)

  def from[T](
    id: Id[T],
  ): Reference[T] =
    Reference(Some(id),None,None,None)

  def from[T](
    extId: ExternalId[T],
  ): Reference[T] =
    Reference(None,Some(extId),None,None)

  
  def to[T <: { def id: Id[T] }](
    t: T,
    display: Option[String] = None
  ): Reference[T] = {
    import scala.language.reflectiveCalls

    Reference.from(t.id).copy(display = display)
  }

  def to[T <: { def id: Id[T] }](
    t: T,
    display: String
  ): Reference[T] = {
    import scala.language.reflectiveCalls

    Reference.to(t,Some(display))
  }


  final case class TypeName[T](value: String)

  object TypeName
  {
    import scala.reflect.ClassTag

    def apply[T](implicit t: TypeName[T]) = t

    implicit def typeName[T](implicit tag: ClassTag[T]): TypeName[T] =
      TypeName[T](tag.runtimeClass.asInstanceOf[Class[T]].getSimpleName)
  }



  implicit def readsReference[T]: Reads[Reference[T]] =
    Json.reads[Reference[T]]
      .filter(
        JsonValidationError("At least one of 'id', 'extId' or 'uri' must be defined on a Reference")
      )(
        ref => ref.id.isDefined || ref.extId.isDefined || ref.uri.isDefined
      )


  implicit def writesReference[T: TypeName]: OWrites[Reference[T]] =
    Json.writes[Reference[T]]
      .transform(
        (js: JsObject) => js + ("type" -> JsString(TypeName[T].value))
      )

}


/*
sealed trait Reference[+T]
{
  self =>

  val display: Option[String]  

  def resolve[TT >: T](implicit res: Resolver[TT]): Option[TT] =
    res(self)

  def resolveOn[TT >: T](
    ts: Iterable[TT]
  )(
    implicit hasId: TT <:< { def id: Id[TT] }
  ): Option[TT] = {
    import scala.language.reflectiveCalls
    self match {
      case IdReference(id,_) => ts.find(_.id == id)
      case _                 => None
    }
  }

//  def resolveF[F,TT >: T](implicit res: ResolverF[F]) = res[TT](self)
}


trait Resolver[T] extends (Reference[T] => Option[T])

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

    Reference(t.id,None)
  }

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

*/

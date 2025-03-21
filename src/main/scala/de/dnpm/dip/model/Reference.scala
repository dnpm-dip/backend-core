package de.dnpm.dip.model


import java.net.URI
//import cats.data.NonEmptyList
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  JsonValidationError,
  Reads,
  OWrites
}


final case class Reference[+T]
(
  id: Option[Id[T]],
  extId: Option[ExternalId[T]],
  uri: Option[URI],
  display: Option[String]
)
{

  def resolve[TT >: T](
    implicit resolver: Reference.Resolver[TT]
  ): Option[TT] =
    resolver(this)

  def resolveOn[TT >: T <: { def id: Id[_] }](
    ts: Iterable[TT]
  ): Option[TT] =
    Reference.Resolver.on(ts)(this)

  def withDisplay(d: String): Reference[T] =
    this.copy(display = Some(d))

}


object Reference
{

  import scala.language.reflectiveCalls

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
  ): Reference[T] =
    Reference.from(t.id).copy(display = display)

  def to[T <: { def id: Id[T] }](
    t: T,
    display: String
  ): Reference[T] =
    Reference.to(t,Some(display))


  final case class TypeName[T](value: String)

  object TypeName
  {
    import scala.reflect.ClassTag

    def apply[T](implicit t: TypeName[T]) = t

    implicit def typeName[T](implicit tag: ClassTag[T]): TypeName[T] =
      TypeName[T](tag.runtimeClass.asInstanceOf[Class[T]].getSimpleName)
  }


  @annotation.implicitNotFound("Couldn't find a Resolver[${T}], define one or ensure it is in implicit scope")
  trait Resolver[T]{
    def apply(ref: Reference[T]): Option[T]
  }

  object Resolver
  {

    implicit def fromFunction[T](f: Reference[T] => Option[T]): Resolver[T] =
      new Resolver[T]{
        override def apply(ref: Reference[T]) = f(ref)
      }
      

    def apply[T,TT >: T](implicit res: Resolver[TT]) = res
/*  
    implicit def on[T,TT >: T <: { def id: Id[_] }](
      implicit ts: Iterable[TT]
    ): Resolver[TT] =
      _.id.flatMap(id => ts.find(_.id == id))
*/
    
    implicit def on[T,TT >: T <: { def id: Id[_] }](
      implicit ts: { def find(f: TT => Boolean): Option[TT] }
    ): Resolver[TT] =
      _.id.flatMap(id => ts.find(_.id == id))
      
  }


  implicit def readsReference[T]: Reads[Reference[T]] =
    Json.reads[Reference[T]]
      .filter(
        JsonValidationError("At least one of 'id', 'extId' or 'uri' must be defined on a Reference")
      )(
        ref => ref.id.isDefined | ref.extId.isDefined | ref.uri.isDefined
      )

  implicit def writesReference[T: TypeName]: OWrites[Reference[T]] =
    Json.writes[Reference[T]]
      .transform(
        (js: JsObject) => js + ("type" -> JsString(TypeName[T].value))
      )

}


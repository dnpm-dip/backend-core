package de.dnpm.dip.model


import java.net.URI
import scala.language.reflectiveCalls
import play.api.libs.json.{
  Json,
  JsObject,
  JsString,
  JsonValidationError,
  Reads,
  OWrites
}
import shapeless.Coproduct
import de.dnpm.dip.coding.Coding


sealed trait Reference[+T]
{

  val id: Id[T]

  def resolveOn[TT >: T <: { def id: Id[_] }](
    ts: { def find(f: TT => Boolean): Option[TT] }
  ): Option[TT] =
     ts.find(_.id == id)


  def resolve[TT >: T](
    implicit resolver: Reference.Resolver[TT]
  ): Option[TT] =
    resolver(this)

  def withDisplay(d: String): Reference[T]
}


final case class InternalReference[+T]
(
  id: Id[T],
  display: Option[String] = None,
  href: Option[URI] = None  // For potential use in a hypermedia-based context, to "augment" the reference with an URI for an API call
)
extends Reference[T]
{

  def withDisplay(d: String) =
    copy(display = Some(d))

  def in[S](implicit sys: Coding.System[S]) =
    ExternalReference[T,S](
      id,
      sys.uri,
      display
    )
}


final case class ExternalReference[+T,+S]
(
  id: Id[T],
  system: URI,               // Identifier of the system in which the ID is defined/resolvable
  display: Option[String] = None,
  href: Option[URI] = None   // For potential use in a hypermedia-based context, to "augment" the reference with an URI for an API call
)
extends Reference[T]
{
  def withDisplay(d: String) =
    copy(display = Some(d))
}


object Reference
{

  def apply[T](
    id: Id[T]
  ): InternalReference[T] =
    InternalReference(id)

  def apply[T,S](
    extId: ExternalId[T,S]
  ): ExternalReference[T,S] =
    ExternalReference(
      Id(extId.value),
      extId.system
    )

  def to[T <: { def id: Id[T] }](
    t: T,
    display: Option[String] = None
  ): InternalReference[T] =
    InternalReference(t.id,display)


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

    implicit def from[T](f: Reference[T] => Option[T]): Resolver[T] =
      new Resolver[T]{
        override def apply(ref: Reference[T]) = f(ref)
      }
      

    def apply[T](implicit res: Resolver[T]) = res
    
    
    implicit def onCollection[T,TT >: T <: { def id: Id[_] }](
      implicit ts: { def find(f: TT => Boolean): Option[TT] }
    ): Resolver[TT] =
      ref => ts.find(_.id == ref.id)
      
  }


  implicit def readsInternalReference[T]: Reads[InternalReference[T]] =
    Json.reads[InternalReference[T]]

  implicit def readsExternalReference[T,S: Coding.System]: Reads[ExternalReference[T,S]] =
    Reads.of[InternalReference[T]]
      .map(_.in[S])

  implicit def readsAnyExternalReference[T]: Reads[ExternalReference[T,Any]] =
    Json.reads[ExternalReference[T,Any]]

  implicit def readsExternalReferenceInSystems[T,S <: Coproduct](
    implicit uris: Coding.System.UriSet[S]
  ): Reads[ExternalReference[T,S]] =
    readsAnyExternalReference[T].asInstanceOf[Reads[ExternalReference[T,S]]]
      .filter(
        JsonValidationError(s"Invalid 'system' value, expected one of {${uris.values.mkString(", ")}}")
      )(
        ref => uris.values.contains(ref.system)
      )


  implicit def readsReference[T]: Reads[Reference[T]] =
    Reads { 
      js => (js \ "system").isDefined match {
        case true  => Json.fromJson[ExternalReference[T,Any]](js)
        case false => Json.fromJson[InternalReference[T]](js)
      }
    }

  implicit def writesInternalReference[T: TypeName]: OWrites[InternalReference[T]] =
    Json.writes[InternalReference[T]]
      .transform(
        (js: JsObject) => js + ("type" -> JsString(TypeName[T].value))
      )

  implicit def writesExternalReference[T: TypeName,S]: OWrites[ExternalReference[T,S]] =
    Json.writes[ExternalReference[T,S]]
      .transform(
        (js: JsObject) => js + ("type" -> JsString(TypeName[T].value))
      )

  implicit def writesReference[T: TypeName]: OWrites[Reference[T]] =
    OWrites { 
      case ref: InternalReference[T]   => Json.toJsObject(ref)
      case ref: ExternalReference[T,_] => Json.toJsObject(ref)
    }

}



final case class CodeableReference[S,T]
(
  coding: Option[Coding[S]],
  reference: Option[Reference[T]]
)

object CodeableReference
{
  implicit def writes[S,T: Reference.TypeName]: OWrites[CodeableReference[S,T]] =
    Json.writes[CodeableReference[S,T]]

  implicit def reads[S: Coding.System,T]: Reads[CodeableReference[S,T]] =
    Json.reads[CodeableReference[S,T]]

  implicit def readsAny[S,T]: Reads[CodeableReference[Any,T]] =
    Json.reads[CodeableReference[Any,T]]
}





/*
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
//    ts: Iterable[TT]
    ts: { def find(f: TT => Boolean): Option[TT] }
  ): Option[TT] =
    Reference.Resolver.onCollection(ts)(this)

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
    
    
    implicit def onCollection[T,TT >: T <: { def id: Id[_] }](
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
*/

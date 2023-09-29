package de.dnpm.dip.coding


import java.net.URI
import cats.{
  Id,
  Applicative
}
import shapeless.Witness
import play.api.libs.json.{
  Json,
  Reads,
  Writes,
  Format,
  JsPath
}
import de.dnpm.dip.util.Completer



final case class Coding[+S]
(
  code: Code[S],
  display: Option[String],
  system: URI,
  version: Option[String]
)
{

  def parent[Spr >: S](
   implicit
   csp: CodeSystemProvider[Spr,cats.Id,Applicative[cats.Id]],
  ): Option[Coding[Spr]] =
    version
      .flatMap(csp.get)
      .getOrElse(csp.latest)
      .parentOf(code)
      .map(p =>
        Coding(
          p.code,
          Some(p.display),
          system,
          p.version
        )
      )

}


object Coding
{

  def apply[E <: Enumeration](
    code: E#Value
  )(
   implicit sys: CodeSystem[E#Value]
  ): Coding[E#Value] =
    Coding[E#Value](
      Code(code.toString),
      Some(Display.of(code).value),
      sys.uri,
      None
    )

  def apply[S](
    code: String
  )(
   implicit sys: System[S]
  ): Coding[S] =
    Coding(
      Code[S](code),
      None,
      sys.uri,
      None
    )

  def apply[S](
    code: String,
    display: String
  )(
   implicit sys: System[S]
  ): Coding[S] =
    Coding(
      Code[S](code),
      Some(display),
      sys.uri,
      None
    )

  def apply[S](
    code: String,
    display: String,
    version: String
  )(
   implicit sys: System[S]
  ): Coding[S] =
    Coding(
      Code[S](code),
      Some(display),
      sys.uri,
      Some(version)
    )

  def apply(
    code: String,
    system: URI
  ): Coding[Any] =
    Coding(
      Code[Any](code),
      None,
      system,
      None
    )

  def apply(
    code: String,
    display: String,
    system: URI
  ): Coding[Any] =
    Coding(
      Code[Any](code),
      Some(display),
      system,
      None
    )

  def apply(
    code: String,
    display: String,
    system: URI,
    version: String
  ): Coding[Any] =
    Coding(
      Code[Any](code),
      Some(display),
      system,
      Some(version)
    )


  @annotation.implicitNotFound("Couldn't find Coding.System instance for ${S}")
  sealed trait System[S]
  {
    def uri: URI
  }

  object System
  {

    def apply[S](s: URI): System[S] =
      new System[S]{
        override val uri = s
      }
  
    def apply[S](s: String): System[S] =
      new System[S]{
        override val uri = URI.create(s)
      }
  
    def apply[S](th: () => String): System[S] =
      new System[S]{
        override lazy val uri = URI.create(th())
      }
  
    def apply[S](implicit sys: System[S]) = sys
  
  
    implicit def enumValueSystem[E <: Enumeration](
      implicit w: Witness.Aux[E]
    ): System[E#Value] =
      System(w.value.getClass.getName.replace("$",""))
  
    implicit def enumSystem[E <: Enumeration](
      implicit w: Witness.Aux[E]
    ): System[E] =
      System(w.value.getClass.getName.replace("$",""))
  
    implicit def codingSystemForValueSet[S](
      implicit vs: ValueSet[S]
    ): System[S] =
      System(vs.uri)

  }


  implicit def completeByCodeSystemProvider[S](
    implicit csp: CodeSystemProvider[S,cats.Id,Applicative[cats.Id]]
  ): Completer[Coding[S]] = 
    Completer.of[Coding[S]](
      coding => 
        coding.version
          .flatMap(csp.get)
          .orElse(Some(csp.latest))
          .flatMap(_.concept(coding.code))
          .map(
            concept =>
              coding.copy(
                display = Some(concept.display),
                version = concept.version
              )   
          )
          .getOrElse(coding)
    )

  implicit def completeByCodeSystem[S](
    implicit cs: CodeSystem[S]
  ): Completer[Coding[S]] = 
    Completer.of[Coding[S]](
      coding => 
        cs.concept(coding.code)
          .map(
            concept =>
              coding.copy(
                display = Some(concept.display),
                version = concept.version
              )   
          )
          .getOrElse(coding)
    )

  implicit def completeByValueSet[S](
    implicit vs: ValueSet[S]
  ): Completer[Coding[S]] = 
    Completer.of[Coding[S]](
      coding => 
        vs.coding(coding.code)
          .map(
            c =>
              coding.copy(
                display = c.display,
                version = c.version
              )   
          )
          .getOrElse(coding)
    )


  import scala.language.implicitConversions

  implicit def fromConcept[S: System](
    concept: CodeSystem.Concept[S]
  ): Coding[S] =
    Coding(
      concept.code,
      Some(concept.display),
      System[S].uri,
      concept.version
    )

  implicit def readsCoding[S: Coding.System]: Reads[Coding[S]] =
    Reads(
      js =>
        for {
          code    <- (js \ "code").validate[Code[S]]
          display <- (js \ "display").validateOpt[String]
          system  <- (js \ "system").validateOpt[URI]
          version <- (js \ "version").validateOpt[String]
        } yield Coding[S](
          code,
          display,
          system.getOrElse(Coding.System[S].uri),
          version
        )
    )

  import play.api.libs.functional.syntax._

  implicit val readsAnyCoding: Reads[Coding[Any]] =
    (
      (JsPath \ "code").read[Code[Any]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "system").read[URI] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,system,version) => Coding[Any](code,display,system,version)
    )


/*
  import play.api.libs.functional.syntax._

  implicit def readsCoding[S]: Reads[Coding[S]] =
    (
      (JsPath \ "code").read[Code[S]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "system").read[URI] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,system,version) => Coding[S](code,display,system,version)
    )
*/


  implicit def writesCoding[S]: Writes[Coding[S]] = 
    Json.writes[Coding[S]]

}



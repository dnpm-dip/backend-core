package de.dnpm.dip.coding


import java.net.URI
import cats.{
  Eq,
  Id,
  Applicative
}
import shapeless.Witness
import play.api.libs.json.{
  Json,
  Reads,
  OWrites,
  OFormat,
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

  override def canEqual(that: Any): Boolean =
    that.isInstanceOf[Coding[_]]

  // Override equals() methods generated for case class, which checks for deep equality
  // because 2 Codings should be considered equal when their code, system (and version??) are equal,
  // irrespective of whether the display value is also defined
  override def equals(that: Any): Boolean =
    that match {

      case that: Coding[_] if that.canEqual(this) =>

        val cdng = that.asInstanceOf[Coding[_]]

        this.system  == cdng.system && 
        this.code    == cdng.code 
        //  && this.version == cdng.version

      case _ => false

    }

  
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

  import de.dnpm.dip.util.{
    Display,
    Displayer
  }


  def apply[E <: Enumeration](
    e: E#Value
  )(
   implicit sys: CodeSystem[E#Value]
  ): Coding[E#Value] = {
    val code = Code[E#Value](e.toString)
    Coding[E#Value](
      code,
      Some(Display.of(code).value),
      sys.uri,
      None
    )
  }

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
  
  }


  implicit def defaultDisplayer[S]: Displayer[Coding[S]] =
    Displayer.from(_.display.getOrElse("N/A"))


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


  import play.api.libs.functional.syntax._

  implicit def readsCoding[S: Coding.System]: Reads[Coding[S]] =
    (
      (JsPath \ "code").read[Code[S]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) => Coding[S](code,display,Coding.System[S].uri,version)
    )

  implicit val readsAnyCoding: Reads[Coding[Any]] =
    (
      (JsPath \ "code").read[Code[Any]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "system").read[URI] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,system,version) => Coding[Any](code,display,system,version)
    )


  implicit def writesCoding[S]: OWrites[Coding[S]] = 
    Json.writes[Coding[S]]



  implicit def eqForCoding[S]: Eq[Coding[S]] =
    Eq.instance(
      (c1,c2) =>
        c1.system  == c2.system && 
        c1.code    == c2.code && 
        c1.version == c2.version 
    )

}

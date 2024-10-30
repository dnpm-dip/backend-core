package de.dnpm.dip.coding


import java.net.URI
import cats.{
  Id,
  Applicative
}
import shapeless.Witness
import shapeless.{
  Coproduct,
  :+:,
  CNil
}
import shapeless.ops.coproduct.Selector
import play.api.libs.json.{
  Json,
  Reads,
  OWrites,
  OFormat,
  JsPath,
  JsonValidationError
}
import de.dnpm.dip.util.{
  Completer,
  Tree
}



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

      case _ => false

    }

  
  def parent[Spr >: S](
    implicit csp: CodeSystemProvider[Spr,Id,Applicative[Id]],
  ): Option[Coding[Spr]] =
    version
      .flatMap(csp.get)
      .getOrElse(csp.latest)
      .parentOf(code)
      .map(_.toCoding(system))

//  @deprecated
  def expand[Spr >: S](
    implicit csp: CodeSystemProvider[Spr,Id,Applicative[Id]]
  ): Option[Tree[Coding[Spr]]] = 
    version
      .flatMap(csp.get)
      .getOrElse(csp.latest)
      .descendantTree(code)
      .map(_.map(_.toCoding(system)))

}


object Coding
{

  import de.dnpm.dip.util.{
    DisplayLabel,
    Displays
  }


  def apply[E <: Enumeration](
    e: E#Value
  )(
   implicit sys: CodeSystem[E#Value]
  ): Coding[E#Value] = {
    val code = Code[E#Value](e.toString)
    Coding[E#Value](
      code,
      Some(DisplayLabel.of(code).value),
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



    // Type class to accumulate URIs for a Coproduct of Systems as a set
    final class UriSet[C] private (val values: Set[URI]) extends AnyVal
    object UriSet
    {
    
      def apply[C](implicit css: UriSet[C]) = css
    
      implicit def coproductUriSet[H, T <: Coproduct](
        implicit
        csH: Coding.System[H],
        cssT: UriSet[T]
      ): UriSet[H :+: T] =
        new UriSet[H :+: T](cssT.values + csH.uri)

      implicit val terminalCoproductUriSet: UriSet[CNil] =
        new UriSet[CNil](Set.empty)
        
    }

    // Type class to accumulate names of the Types in Coproduct of Systems
    final class Names[C] private (val names: List[String]) extends AnyVal
    object Names
    {
      import scala.reflect.ClassTag
    
      def apply[C](implicit names: Names[C]) = names
    
      implicit def coproductNameSet[H, T <: Coproduct](
        implicit
        ctH: ClassTag[H],
        tail: Names[T]
      ): Names[H :+: T] =
        new Names[H :+: T](ctH.runtimeClass.getSimpleName :: tail.names)

      implicit val cnilNames: Names[CNil] =
        new Names[CNil](List.empty)
        
    }

  }


  implicit def descendantExpander[T](
    implicit csp: CodeSystemProvider[T,Id,Applicative[Id]]
  ): Tree.Expander[Coding[T]] =
    coding =>
      coding.version
        .flatMap(csp.get)
        .getOrElse(csp.latest)
        .descendantTree(coding.code)
        .map(_.map(_.toCoding(coding.system)))
        .getOrElse(Tree(coding))


  implicit def defaultDisplays[S]: Displays[Coding[S]] =
    Displays[Coding[S]](_.display.getOrElse("N/A"))


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


//  implicit def writesCoding[S]: OWrites[Coding[S]] = 
//    Json.writes[Coding[S]]

  implicit val writesAnyCoding: OWrites[Coding[Any]] = 
    Json.writes[Coding[Any]]

  implicit def writesCoproductCoding[S <: Coproduct]: OWrites[Coding[S]] =
    writesAnyCoding.contramap(_.asInstanceOf[Coding[Any]])

  implicit def writesCoding[S]: OWrites[Coding[S]] = 
    (
      (JsPath \ "code").write[Code[S]] and
      (JsPath \ "display").writeNullable[String] and
      (JsPath \ "version").writeNullable[String]
    )(
      coding => (coding.code,coding.display,coding.version)
    )


  implicit def readsEnumCoding[
    E <: Enumeration
  ](
    implicit cs: CodeSystem[E#Value]
  ): Reads[Coding[E#Value]] =
    (
      (JsPath \ "code").read[Code[E#Value]].filter(
        JsonValidationError(
          s"Invalid 'code' value, expected one of {${cs.concepts.map(_.code.value).mkString(", ")}}"
        )
      )(
        code => cs.concepts.exists(_.code.value == code.value)
      ) and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) =>
        Coding[E#Value](
          code,
          display,
          cs.uri,
          version
        )
    )


  implicit def readsCodingByCodeSystem[S](
    implicit cs: CodeSystem[S]
  ): Reads[Coding[S]] =
    (
      (JsPath \ "code").read[Code[S]]
        .filter(
          JsonValidationError(
            s"Invalid value, expected one of {${cs.concepts.map(_.code.value).mkString(", ")}}"
          )
        )(
          code => cs.concepts.exists(_.code.value == code.value)
        ) and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) =>
        Coding[S](
          code,
          display,
          cs.uri,
          version
        )
    )

/*
  implicit def readsEnumCoding[
    E <: Enumeration
  ](
    implicit cs: CodeSystem[E#Value]
  ): Reads[Coding[E#Value]] =
    (
      (JsPath \ "code").read[Code[E#Value]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) =>
        Coding[E#Value](
          code,
          display,
          cs.uri,
          version
        )
    )
    .filter(
      JsonValidationError(
        s"Invalid 'code' value, expected one of {${cs.concepts.map(_.code.value).mkString(", ")}}"
      )
    )(
      coding => cs.concepts.exists(_.code.value == coding.code.value)
    )


  implicit def readsCodingByCodeSystem[S](
    implicit cs: CodeSystem[S]
  ): Reads[Coding[S]] =
    (
      (JsPath \ "code").read[Code[S]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) =>
        Coding[S](
          code,
          display,
          cs.uri,
          version
        )
    )
    .filter(
      JsonValidationError(
        s"Invalid 'code' value, expected one of {${cs.concepts.map(_.code.value).mkString(", ")}}"
      )
    )(
      coding => cs.concepts.exists(_.code.value == coding.code.value)
    )
*/

  implicit def readsCoding[S: Coding.System]: Reads[Coding[S]] =
    (
      (JsPath \ "code").read[Code[S]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,version) =>
        Coding[S](
          code,
          display,
          Coding.System[S].uri,
          version
        )
    )


  implicit val readsAnyCoding: Reads[Coding[Any]] =
    (
      (JsPath \ "code").read[Code[Any]] and
      (JsPath \ "display").readNullable[String] and
      (JsPath \ "system").read[URI] and
      (JsPath \ "version").readNullable[String]
    )(
      (code,display,system,version) =>
        Coding[Any](
          code,
          display,
          system,
          version
        )
    )

  implicit def readsCoproductCoding[S <: Coproduct](
    implicit css: System.UriSet[S]
  ): Reads[Coding[S]] =
    readsAnyCoding.asInstanceOf[Reads[Coding[S]]]
      .filter(
        JsonValidationError(s"Invalid 'system' value, expected one of {${css.values.mkString(", ")}}")
      )(
        coding => css.values.contains(coding.system)
      )


  implicit def codingToCoproductCoding[S, C <: Coproduct](
    coding: Coding[S]
  )(
    implicit sel: Selector[C,S]
  ): Coding[C] =
    coding.asInstanceOf[Coding[C]]

}

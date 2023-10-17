package de.dnpm.dip.coding


import java.net.URI
import java.time.LocalDateTime
import cats.Eval
import play.api.libs.json.{
  Json,
  Format
}
import scala.collection.{
  WithFilter => StdWithFilter
}


final case class CodeSystem[S]
(
  uri: URI,
  name: String,
  title: Option[String],
  date: Option[LocalDateTime],
  version: Option[String],
  properties: List[CodeSystem.Property],
  concepts: Seq[CodeSystem.Concept[S]]
)
{

  self =>


  final class WithFilter(
    private val wf: StdWithFilter[CodeSystem.Concept[S],Seq]
  )
  {
    def value: CodeSystem[S] =
      self.copy( concepts = wf map identity )

    def withFilter(f2: CodeSystem.Concept[S] => Boolean): WithFilter =
      new WithFilter(wf.withFilter(f2))
  }


  def concept(c: Code[S]): Option[CodeSystem.Concept[S]] =
    this.concepts.find(_.code == c)

  def conceptWithCode(c: String): Option[CodeSystem.Concept[S]] =
    this.concept(Code[S](c))

  def concept[E <: Enumeration](
    c: S
  )(
    implicit isEnum: S =:= E#Value
  ): CodeSystem.Concept[S] =
    this.conceptWithCode(c.toString).get


  def coding(c: Code[S]): Option[Coding[S]] =
    this.concept(c)
      .map(_.toCoding(this.uri))

  def codingWithCode(c: String): Option[Coding[S]] =
    this.conceptWithCode(c)
      .map(_.toCoding(this.uri))

  def coding[E <: Enumeration](
    c: S
  )(
    implicit isEnum: S =:= E#Value
  ): Coding[S] =
    this.codingWithCode(c.toString).get


  def parentOf(c: Code[S]): Option[CodeSystem.Concept[S]] =
    concept(c)
      .flatMap(_.parent)
      .flatMap(concept)


  def childrenOf(p: Code[S]): Seq[CodeSystem.Concept[S]] =
    concept(p)
      .flatMap(_.children)
      .map(_.flatMap(concept))
      .getOrElse(Seq.empty)


  def childrenOf(p: CodeSystem.Concept[S]): Seq[CodeSystem.Concept[S]] =
    childrenOf(p.code)


  //TODO: Look for tail-recursive implementation?
  def descendantsOf(p: Code[S]): Seq[CodeSystem.Concept[S]] = { 

    val children = childrenOf(p)
            
    children ++ children.flatMap(c => childrenOf(c.code))
  }

  def descendantsOf(p: CodeSystem.Concept[S]): Seq[CodeSystem.Concept[S]] =
    descendantsOf(p.code)


  def displayOf(c: Code[S]): Option[String] =
    this.concept(c).map(_.display)


  def filter(f: CodeSystem.Concept[S] => Boolean): CodeSystem[S] =
    this.copy(concepts = concepts.filter(f))
  
  def withFilter(f: CodeSystem.Concept[S] => Boolean): WithFilter =
    new WithFilter(concepts.withFilter(f))

}

object CodeSystem
{

  final case class Info
  (
    name: String,
    title: Option[String],
    uri: URI,
    version: Option[String]
  )


  final case class Property private (
    name: String,
    `type`: String,
    description: Option[String],
    valueSet: Option[Set[String]]  
  )

  object Property
  {

    @annotation.implicitNotFound("${T} is not a valid CodeSystem.Property type")
    sealed abstract class Type[T]{ val name: String }

    object Type
    {
      def apply[T](implicit t: Type[T]) = t


      private def apply[T](n: String): Type[T] =
        new Type[T]{ val name = n }

      implicit val integer: Type[Int] =
        Type[Int]("integer")

      implicit val decimal: Type[Double] =
        Type[Double]("decimal")

      implicit val boolean: Type[Boolean] =
        Type[Boolean]("boolean")

      implicit val string: Type[String] =
        Type[String]("string")

    }

    def apply[T](
      name: String,
      description: Option[String],
      valueSet: Option[Set[String]] = None
    )(
      implicit typ: Type[T]
    ): Property =
      Property(
        name,
        typ.name,
        description,
        valueSet
      )


    def apply[E <: Enumeration](
      enumeration: E,
      name: String,
      description: Option[String],
    ): Property =
      Property(
        name,
        "enum",
        description,
        Some(enumeration.values.map(_.toString))
      )

  }

 
  final case class Concept[S]
  (
    code: Code[S],
    display: String,
    version: Option[String],
    properties: Map[String,Set[String]],
    parent: Option[Code[S]],
    children: Option[List[Code[S]]] 
  )
  {
    def get(p: Property): Option[Set[String]] =
      properties.get(p.name)

    def toCoding(uri: URI): Coding[S] =
      Coding[S](
        code,
        Some(display),
        uri,
        version
      )

    def toCoding(implicit cs: Coding.System[S]): Coding[S] =
      this.toCoding(cs.uri)


  }



  abstract class Filter[T] private (
    val name: String,
    val description: Option[String]
  )
  extends (Concept[T] => Boolean)
  {
    self =>

    def and(other: Filter[T]): Filter[T] =
      Filter(
        s"${self.name} AND ${other.name}",
        for {
          d1 <- self.description
          d2 <- other.description
        } yield s"$d1 AND $d2",
        c => self(c) && other(c)
      )

    def or(other: Filter[T]): Filter[T] =
      Filter(
        s"${self.name} OR ${other.name}",
        for {
          d1 <- self.description
          d2 <- other.description
        } yield s"$d1 OR $d2",
        c => self(c) || other(c)
      )

    def &&(other: Filter[T]) = self and other 

    def ||(other: Filter[T]) = self or other 

  }

  object Filter
  {

    def apply[T](
      name: String,
      description: Option[String],
      op: Concept[T] => Boolean
    ): Filter[T] =
      new Filter[T](
        name,
        description
      ){
        override def apply(c: Concept[T]) = op(c)
      }

  }


  def apply[S](implicit cs: CodeSystem[S]) = cs


  def of[T](
    uri: URI,
    name: String,
    title: Option[String],
    version: Option[String],
    concepts: (T,String)*
  ): CodeSystem[T] =
    CodeSystem(
      uri,
      name,
      title,
      None,
      None,
      List.empty,
      concepts.toSeq.map {
        case (t,display) =>
          Concept[T]( 
            Code[T](t.toString),
            display,
            version,
            Map.empty,
            None,
            None
        )
      }
    )


  def apply[T](
    uri: URI,
    name: String,
    title: Option[String],
    version: Option[String],
    concepts: (String,String)*
  ): CodeSystem[T] =
    CodeSystem(
      uri,
      name,
      title,
      None,
      None,
      List.empty,
      concepts.toSeq.map {
        case (c,display) =>
          Concept[T]( 
            Code[T](c),
            display,
            version,
            Map.empty,
            None,
            None
        )
      }
    )


  def apply[T: Coding.System](
    name: String,
    title: Option[String],
    version: Option[String],
    concepts: (String,String)*
  ): CodeSystem[T] =
    CodeSystem(
      Coding.System[T].uri,
      name,
      title,
      None,
      None,
      List.empty,
      concepts.toSeq.map {
        case (c,display) =>
          Concept[T]( 
            Code[T](c),
            display,
            version,
            Map.empty,
            None,
            None
        )
      }
    )


  trait Publisher[T]
  {

    implicit val codingSystem: Coding.System[T]

    val properties: List[Property]

    val filters: List[Filter[T]]
  }



  implicit val formatInfo: Format[Info] =
    Json.format[Info]

  implicit val formatProperty: Format[Property] =
    Json.format[Property]

  implicit def formatConcept[S]: Format[Concept[S]] =
    Json.format[Concept[S]]

  implicit def formatCodeSystem[S]: Format[CodeSystem[S]] =
    Json.format[CodeSystem[S]]


  import scala.language.implicitConversions

  implicit def toAnyCodeSystem[S,T >: S](cs: CodeSystem[S]): CodeSystem[T] =
    cs.asInstanceOf[CodeSystem[T]]

  implicit def toInfo[S](cs: CodeSystem[S]): Info =
    Info(
      cs.name,
      cs.title,
      cs.uri,
      cs.version
    )

}



@annotation.implicitNotFound(
"Couldn't resolve implicit CodeSystems for default injection. Ensure implicit CodeSystems are in scope for all types in ${CS}."
)
trait CodeSystems[CS]{
  val values: List[CodeSystem[Any]]
}

object CodeSystems
{

  import shapeless.{HList, ::, HNil, Generic}

  def apply[CS <: Product](implicit cs: CodeSystems[CS]) = cs

  implicit def genericCodeSystem[CS, CSpr](
    implicit
    gen: Generic.Aux[CS,CSpr],
    genCs: CodeSystems[CSpr]
  ): CodeSystems[CS] =
    new CodeSystems[CS]{
      val values = genCs.values
    }

  implicit def hlistCodeSystems[H, T <: HList](
    implicit
    hcs: CodeSystem[H],
    tcs: CodeSystems[T]
  ): CodeSystems[H :: T] =
    new CodeSystems[H :: T]{
      val values = hcs :: tcs.values
    }

  implicit def productHeadHListCodeSystems[H <: Product, T <: HList](
    implicit
    hcs: CodeSystems[H],
    tcs: CodeSystems[T]
  ): CodeSystems[H :: T] =
    new CodeSystems[H :: T]{
      val values = hcs.values ++ tcs.values
    }

  implicit val hnilCodeSystems: CodeSystems[HNil] =
    new CodeSystems[HNil]{
      val values = Nil
    }

}


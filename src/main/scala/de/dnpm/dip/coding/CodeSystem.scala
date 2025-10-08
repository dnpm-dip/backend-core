package de.dnpm.dip.coding


import java.net.URI
import java.time.LocalDateTime
import scala.util.chaining._
import scala.collection.{
  WithFilter => StdWithFilter
}
import scala.collection.concurrent.{
  Map => MutableMap,
  TrieMap
}
import play.api.libs.json.{
  Json,
  JsPath,
  OFormat,
  Writes,
  OWrites
}
import shapeless.Coproduct
import shapeless.ops.coproduct.Selector
import de.dnpm.dip.util.Tree


final case class CodeSystem[S]
(
  uri: URI,
  name: String,
  title: Option[String],
  date: Option[LocalDateTime],
  version: Option[String],
  properties: List[CodeSystem.Property],
  concepts: Seq[CodeSystem.Concept[S]],
  // Allow specifying a custom look-up function for Concept by Code:
  // By default, look-up is by exact code equality, but some CodeSystems may require some custom logic,
  // e.g. fuzzy matching or in ICD-10, which allows code "modifiers", a concept could be looked up by modified code
  // (see below in def concept(...))
  customConceptLookup: Option[(Code[S] => Option[CodeSystem.Concept[S]])] = None
)
{

  self =>


  final class WithFilter(
    private val wf: StdWithFilter[CodeSystem.Concept[S],Seq]
  )
  {
    def value: CodeSystem[S] =
      self.copy(concepts = wf map identity )

    def withFilter(f2: CodeSystem.Concept[S] => Boolean): WithFilter =
      new WithFilter(wf.withFilter(f2))
  }


  private val conceptMap: MutableMap[Code[S],CodeSystem.Concept[S]] =
    TrieMap.from(
      concepts.map(c => (c.code, c))
    )

  private lazy val descendantTrees: MutableMap[Code[S],Tree[CodeSystem.Concept[S]]] =
    TrieMap.empty



  def concept(code: Code[S]): Option[CodeSystem.Concept[S]] = 
    customConceptLookup match {
      case None         => conceptMap.get(code)
      case Some(lookup) => conceptMap.get(code) orElse lookup(code).tap(_.foreach(c => conceptMap += c.code -> c)) // update the conceptMap for faster subsequent lookup
    }


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

  def parentOf(c: CodeSystem.Concept[S]): Option[CodeSystem.Concept[S]] =
    parentOf(c.code)


  def parentsOf(
    c: Code[S],
    // allow providing a function to determine parents of a concept
    // since not all hierarchical CodeSystems are pure trees, but allow a concept
    // to have multiple parents/super-concepts, so this could be the function to
    // get the corresponding concept property
    parents: CodeSystem.Concept[S] => Set[Code[S]] = parentOf(_).map(_.code).toSet
  ): Set[CodeSystem.Concept[S]] =
    concept(c)
      .toSet
      .flatMap(parents)
      .flatMap(concept)


  def ancestorsOf(
    c: Code[S],
    // allow providing a function to determine parents of a concept
    // since not all hierarchical CodeSystems are pure trees, but allow a concept
    // to have multiple parents/super-concepts, so this could be the function to
    // get the corresponding concept property
    ps: CodeSystem.Concept[S] => Set[Code[S]] = c => parentsOf(c.code).map(_.code)
  ): Set[CodeSystem.Concept[S]] = {

    val parents = parentsOf(c,ps)

    //TODO: Look for tail-recursive implementation?
    parents ++
      parents.flatMap(p => ancestorsOf(p.code,ps))

  }


  def childrenOf(p: Code[S]): Set[CodeSystem.Concept[S]] =
    concept(p)
      .flatMap(_.children)
      .map(_.flatMap(concept))
      .getOrElse(Set.empty)


  def childrenOf(p: CodeSystem.Concept[S]): Set[CodeSystem.Concept[S]] =
    childrenOf(p.code)


  @deprecated("Might be removed in favour of method CodeSystem.descendants returning Tree","")
  def descendantsOf(p: Code[S]): Set[CodeSystem.Concept[S]] = { 

    val children = childrenOf(p)
            
    //TODO: Look for tail-recursive implementation?
    children ++
      children.flatMap(descendantsOf)
  }

  @deprecated("Might be removed in favour of method CodeSystem.descendants returning Tree","")
  def descendantsOf(p: CodeSystem.Concept[S]): Set[CodeSystem.Concept[S]] =
    descendantsOf(p.code)


  def descendants(code: Code[S]): Option[Tree[CodeSystem.Concept[S]]] =
    concept(code)
      .map(
        cpt =>
          descendantTrees.getOrElseUpdate(
            cpt.code,
            Tree(
              cpt,
              Option(childrenOf(cpt).toSeq.flatMap(c => descendants(c.code)))
                .filter(_.nonEmpty)
            )
          )
      )


  def displayOf(c: Code[S]): Option[String] =
    this.concept(c).map(_.display)

  def filter(f: CodeSystem.Concept[S] => Boolean): CodeSystem[S] =
    this.copy(concepts = concepts.filter(f))
  
  def withFilter(f: CodeSystem.Concept[S] => Boolean): WithFilter =
    new WithFilter(concepts.withFilter(f))

}


object CodeSystem
{

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
    children: Option[Set[Code[S]]] 
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

    def toCodingOf[T <: Coproduct](
      implicit
      cs: Coding.System[S],
      sel: Selector[T,S]
    ): Coding[T] =
      this.toCoding.asInstanceOf[Coding[T]]
  }

  object Concept
  {

    def properties(
      prop: (Property,Iterable[String]),
      props: (Property,Iterable[String])*,
    ): Map[String,Set[String]] =
      (prop +: props)
        .collect {
          case (prp,values) if values.nonEmpty => prp.name -> values.toSet
        }
        .toMap

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

    implicit def writes[T]: OWrites[Filter[T]] =
      OWrites {
        filter =>
          Json.obj(
            "name"        -> filter.name,
            "description" -> filter.description
          )
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
      uri        = uri,
      name       = name,
      title      = title,
      date       = None,
      version    = version,
      properties = List.empty,
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
      uri        = uri,
      name       = name,
      title      = title,
      date       = None,
      version    = version,
      properties = List.empty,
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
      uri        = Coding.System[T].uri,
      name       = name,
      title      = title,
      date       = None,
      version    = version,
      properties = List.empty,
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

    def properties: List[Property]

    def filters: List[Filter[T]]
  }


  object Publisher
  {
    def apply[T](implicit csp: Publisher[T]): Publisher[T] = csp
  }


  implicit val formatProperty: OFormat[Property] =
    Json.format[Property]

  implicit def formatConcept[S]: OFormat[Concept[S]] =
    Json.format[Concept[S]]

//  implicit def formatCodeSystem[S]: OFormat[CodeSystem[S]] =
//    Json.format[CodeSystem[S]]


  import play.api.libs.functional.syntax._

  implicit def writeCodeSystem[S, C <: CodeSystem[S]]: Writes[C] =
    (
      (JsPath \ "uri").write[URI] and
      (JsPath \ "name").write[String] and
      (JsPath \ "title").writeNullable[String] and
      (JsPath \ "date").writeNullable[LocalDateTime] and
      (JsPath \ "version").writeNullable[String] and
      (JsPath \ "properties").write[List[CodeSystem.Property]] and
      (JsPath \ "concepts").write[Seq[CodeSystem.Concept[S]]]
    )(
      cs => (
        cs.uri,
        cs.name,
        cs.title,
        cs.date,
        cs.version,
        cs.properties,
        cs.concepts
      ) 
    )


  implicit def toAnyCodeSystem[S,T >: S](cs: CodeSystem[S]): CodeSystem[T] =
    cs.asInstanceOf[CodeSystem[T]]

}


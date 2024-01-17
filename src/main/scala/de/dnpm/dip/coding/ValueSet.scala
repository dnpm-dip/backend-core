package de.dnpm.dip.coding


import java.net.URI
import java.time.LocalDateTime
import scala.util.matching.Regex
import cats.Applicative
import play.api.libs.json.{
  Json,
  OWrites,
  Reads,
  OFormat
}


//final case class ValueSet[+S]
final case class ValueSet[S]
(
  uri: URI,
  name: String,
  title: Option[String],
  date: Option[LocalDateTime],
  version: Option[String],
  codings: Seq[Coding[S]]
)
{

/*
  def coding[Spr >: S](c: Code[Spr]): Option[Coding[Spr]] =
    this.codings.find(_.code == c)

  def displayOf[Spr >: S](c: Code[Spr]): Option[String] =
    this.coding(c).flatMap(_.display)
*/

  def coding(c: Code[S]): Option[Coding[S]] =
    this.codings.find(_.code == c)

  def displayOf(c: Code[S]): Option[String] =
    this.coding(c).flatMap(_.display)

  def include[T >: S,U >: T](
    cs: Seq[Coding[T]]
  ): ValueSet[U] =
    this.copy(
      date = Some(LocalDateTime.now),
      codings =
        codings.asInstanceOf[Seq[Coding[U]]] ++
          cs.asInstanceOf[Seq[Coding[U]]]
    )

}


object ValueSet
{

  final case class Info
  (
    name: String,
    title: Option[String],
    uri: URI,
    version: Option[String]
  )


  trait Composer
  {
    self =>

    val uri: URI

    val name: String

    val title: Option[String]

    def include[S](
     codings: Coding[S]*,
    ): Composer

    def include(
      system: URI,
      filter: CodeSystem.Concept[Any] => Boolean
    ): Composer

    def include[S](
      filter: CodeSystem.Concept[Any] => Boolean
    )(
      implicit sys: Coding.System[S]
    ): Composer =
      self.include(
        sys.uri,
        filter
      )

    def includeAll[S](
      implicit sys: Coding.System[S]
    ): Composer =
      self.include(
        sys.uri,
        (c: CodeSystem.Concept[Any]) => true
      )


    def include(
      system: URI,
      codes: String*
    ): Composer =
      self.include(
        system,
        c => codes.toSet.contains(c.code.value)
      )

    def include[S](
      codes: String*
    )(
      implicit sys: Coding.System[S]
    ): Composer =
      self.include(
        sys.uri,
        c => codes.toSet.contains(c.code.value)
      )

    def include(
      system: URI,
      regex: Regex
    ): Composer =
      self.include(
        system,
        c => regex.matches(c.code.value)
      )


    def exclude(
      system: URI,
      filter: CodeSystem.Concept[Any] => Boolean
    ): Composer

    def exclude[S](
      filter: CodeSystem.Concept[Any] => Boolean
    )(
      implicit sys: Coding.System[S]
    ): Composer =
      self.exclude(
        sys.uri,
        filter
      )

    def exclude(
      system: URI,
      codes: String*
    ): Composer =
      self.exclude(
        system,
        c => codes.toSet.contains(c.code.value)
      )

    def exclude[S](
      codes: String*
    )(
      implicit sys: Coding.System[S]
    ): Composer =
      self.exclude(
        sys.uri,
        c => codes.toSet.contains(c.code.value)
      )

    def exclude(
      system: URI,
      regex: Regex
    ): Composer =
      self.exclude(
        system,
        c => regex.matches(c.code.value)
      )


    def expand(
      css: CodeSystem[Any]*
    ): ValueSet[Any]


    def expand[CS <: Product](
      implicit
      cs: CodeSystems[CS]
    ): ValueSet[Any] =
      self.expand(
        cs.values: _*
      )

    def expand[S](
      csp: CodeSystemProvider[S,cats.Id,Applicative[cats.Id]]
    ): ValueSetProvider[Any,cats.Id,Applicative[cats.Id]] = {
      new LazyValueSetProvider[Any,cats.Id](csp,self)
    }

  }


  implicit class PredicateOps[T](val f: T => Boolean) extends AnyVal
  {
    def and(g: T => Boolean): T => Boolean = 
      t => f(t) && g(t)

    def or(g: T => Boolean): T => Boolean = 
      t => f(t) || g(t)
  }


  private case class ComposerImpl private [coding] (
    uri: URI,
    name: String,
    title: Option[String],
    codings: Seq[Coding[Any]],
    incls: Map[URI,CodeSystem.Concept[Any] => Boolean],
    excls: Map[URI,CodeSystem.Concept[Any] => Boolean]
  )
  extends Composer
  {

    override def include[S](
     cdngs: Coding[S]*,
    ): Composer =
      this.copy(
        codings = codings :++ cdngs
      )
      

    override def include(
      system: URI,
      filter: CodeSystem.Concept[Any] => Boolean
    ): Composer =
      this.copy(
        incls =
          incls.updatedWith(system){
            case Some(f) => Some(f or filter)
            case None    => Some(filter)
          }
      )

    override def exclude(
      system: URI,
      filter: CodeSystem.Concept[Any] => Boolean
    ): Composer =
      this.copy(
        excls =
          excls.updatedWith(system){
            case Some(f) => Some(f or filter)
            case None    => Some(filter)
          }
      )


    override def expand(
      css: CodeSystem[Any]*
    ): ValueSet[Any] = {
      val cdngs =
        incls.foldLeft(
          Seq.empty[Coding[Any]]
        ){
          case (acc,(sys,incl)) =>
            val cs =
              css.find(_.uri == sys).get
              
            acc ++
              cs.concepts
                .withFilter(incl)
                .map(_.toCoding(cs.uri))

        } ++
        excls.foldLeft(
          Seq.empty[Coding[Any]]
        ){
          case (acc,(sys,excl)) =>
            val cs =
              css.find(_.uri == sys).get
              
            acc ++
              cs.concepts
                .filterNot(excl)
                .map(_.toCoding(cs.uri))
        } ++ codings

      ValueSet(
        uri,
        name,
        title,
        Some(LocalDateTime.now),
//        version,
        None,
        cdngs.distinctBy(_.code)
      )
    }

  }


  def compose(
    uri: URI,
    name: String,
    title: Option[String]
  ): Composer =
    new ComposerImpl(
      uri,
      name,
      title,
      Seq.empty,
      Map.empty,
      Map.empty
    )


  def from[S](
    cs: CodeSystem[S]
  ): ValueSet[S] =
    ValueSet
      .compose(
        cs.uri,
        cs.name,
        cs.title
      )
      .include(
        cs.uri,
        _ => true
      )
      .expand(cs)
      .asInstanceOf[ValueSet[S]]


  implicit def writesValueSet[S]: OWrites[ValueSet[S]] = 
    Json.writes[ValueSet[S]]

}


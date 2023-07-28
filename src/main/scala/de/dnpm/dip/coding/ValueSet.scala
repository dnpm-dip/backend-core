package de.dnpm.dip.coding


import java.net.URI
import java.time.LocalDateTime
import scala.util.matching.Regex
import play.api.libs.json.{
  Json, Writes, Reads, Format
}


final case class ValueSet[S]
(
  uri: URI,
  name: String,
  title: Option[String],
  date: Option[LocalDateTime],
  version: Option[String],
  concepts: Seq[Coding[S]]
)
{

  def concept(c: Code[S]): Option[Coding[S]] =
    this.concepts.find(_.code == c)

  def displayOf(c: Code[S]): Option[String] =
    this.concept(c).flatMap(_.display)

  def include[T >: S,U >: T](
    cs: Seq[Coding[T]]
  ): ValueSet[U] =
    this.copy(
      date = Some(LocalDateTime.now),
      concepts =
        concepts.asInstanceOf[Seq[Coding[U]]] ++
          cs.asInstanceOf[Seq[Coding[U]]]
    )

}


object ValueSet
{

  final case class Info
  (
//    name: String,
    title: Option[String],
    uri: URI,
    version: Option[String]
  )


  trait Composer
  {
    self =>

    def include(
      system: URI,
      filter: CodeSystem.Concept[_] => Boolean
    ): Composer

    def include[S](
      filter: CodeSystem.Concept[_] => Boolean
    )(
      implicit sys: Coding.System[S]
    ): Composer =
      self.include(
        sys.uri,
        filter
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
      filter: CodeSystem.Concept[_] => Boolean
    ): Composer

    def exclude[S](
      filter: CodeSystem.Concept[_] => Boolean
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

    def expand[S](
      uri: URI,
      name: String,
      title: Option[String], 
      version: Option[String],
      css: CodeSystem[_]*
    ): ValueSet[S]


    def expand[CS <: Product](
      uri: URI,
      name: String,
      title: Option[String], 
      version: Option[String],
    )(
      implicit
      cs: CodeSystems[CS]
    ): ValueSet[Any] =
      self.expand(
        uri,
        name,
        title,
        version,
        cs.values: _*
      )

  }


  implicit class PredicateOps[T](val f: T => Boolean) extends AnyVal
  {
    def and(g: T => Boolean): T => Boolean = 
      t => f(t) && g(t)

    def or(g: T => Boolean): T => Boolean = 
      t => f(t) || g(t)
  }


  import scala.collection.mutable.Map

  private class ComposerImpl private [coding] (
    val incls: Map[URI,CodeSystem.Concept[_] => Boolean],
    val excls: Map[URI,CodeSystem.Concept[_] => Boolean]
  )
  extends Composer
  {

    override def include(
      system: URI,
      filter: CodeSystem.Concept[_] => Boolean
    ): Composer = {
      val incl =
        incls.get(system)
          .map(_ or filter)
          .getOrElse(filter)

      incls.update(system, incl)
      this 
    }

    override def exclude(
      system: URI,
      filter: CodeSystem.Concept[_] => Boolean
    ): Composer = {
      val excl =
        excls.get(system)
          .map(_ or filter)
          .getOrElse(filter)

      excls.update(system, excl)
      this 
    }


    override def expand[S](
      uri: URI,
      name: String,
      title: Option[String], 
      version: Option[String],
      css: CodeSystem[_]*
    ): ValueSet[S] = {
      val codings =
        incls.foldLeft(
          Seq.empty[Coding[S]]
        ){
          case (acc,(sys,incl)) =>
            val cs =
              css.find(_.uri == sys).get
              
            acc ++
              cs.concepts
                .filter(incl)
                .map( c =>
                  Coding[S](
                    Code[S](c.code.value),
                    Some(c.display),
                    cs.uri,
                    cs.version
                  )
                )

        } ++
        excls.foldLeft(
          Seq.empty[Coding[S]]
        ){
          case (acc,(sys,excl)) =>
            val cs =
              css.find(_.uri == sys).get
              
            acc ++
              cs.concepts
                .filterNot(excl)
                .map( c =>
                  Coding[S](
                    Code[S](c.code.value),
                    Some(c.display),
                    cs.uri,
                    cs.version
                  )
                )
        }

      ValueSet(
        uri,
        name,
        title,
        Some(LocalDateTime.now),
        version,
        codings.distinctBy(_.code)
      )
    }

  }


  def compose: Composer =
    new ComposerImpl(Map.empty,Map.empty)



  implicit def formatValueSet[S] = Json.format[ValueSet[S]]

}


package de.dnpm.dip.coding.atc


import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


object Kinds extends Enumeration
{
  val Group     = Value
  val Substance = Value
}


sealed trait ATC
object ATC extends CodeSystem.Publisher[ATC]
{

  implicit val codingSystem: Coding.System[ATC] =
    Coding.System[ATC]("http://fhir.de/CodeSystem/bfarm/atc")


  val Kind =
    CodeSystem.Property(
      Kinds,
      "kind",
      Some("Kind of entry")
    )

  val DDD =
    CodeSystem.Property[String](
      "DDD",
      Some("Defined Daily Dose"),
      None
    )


  val filterByKind: Map[Kinds.Value,CodeSystem.Filter[ATC]] =
    Kinds.values
      .toList
      .map { kind =>

        import extensions._

        kind -> CodeSystem.Filter[ATC](
          s"is a $kind",
          Some(s"Filter entries with 'kind' = '$kind'"),
          _.kind == kind
        )
      }
      .toMap



  object extensions
  {

    implicit class ATCConceptProperties(val c: CodeSystem.Concept[ATC]) extends AnyVal
    {

      def kind: Kinds.Value =
        Kinds.withName(c.get(Kind).get.head)

      def ddd: Option[String] =
        c.get(DDD).flatMap(_.headOption)
    }

    implicit class ATCCodingProperties(val coding: Coding[ATC]) extends AnyVal
    {
      import Kinds._

      def group(
        implicit csp: CodeSystemProvider[ATC,Id,Applicative[Id]]
      ): Option[Coding[ATC]] =
        for {
          cs <- csp.get(coding.version.getOrElse(csp.latestVersion))
          concept <- cs.concept(coding.code)
          group <- 
            concept.kind match {
              case Substance => cs.parentOf(concept)
              case Group     => Some(concept)
            }
        } yield group.toCoding
    }

  }


  override val properties =
    List(
      Kind,
      DDD
    )  

  override val filters =
    filterByKind.values.toList



  trait Catalogs[F[_],Env] extends CodeSystemProvider[ATC,F,Env]


  trait CatalogsSPI extends SPIF[
    ({ type Service[F[_]] = Catalogs[F,Applicative[F]] })#Service
  ]


  object Catalogs extends SPILoaderF[CatalogsSPI]

}

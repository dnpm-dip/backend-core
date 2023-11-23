package de.dnpm.dip.coding.ask


import cats.Applicative
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



sealed trait ASK


object ASK extends CodeSystem.Publisher[ASK]
{

  implicit val codingSystem: Coding.System[ASK] =
    Coding.System[ASK]("http://fhir.de/CodeSystem/bfarm/ask")


  val CASNumber =
    CodeSystem.Property[String](
      "CAS-Number",
      Some("Chemical Abstract Service (CAS) Number")
    )

  val INN =
    CodeSystem.Property[String](
      "INN",
      Some("International Nonproprietary Name (INN)")
    )

  val TradeNames =
    CodeSystem.Property[String](
      "TradeNames",
      Some("Trade Names")
    )

  val Synonyms =
    CodeSystem.Property[String](
      "Synonyms",
      Some("Synonyms")
    )

  val Formula =
    CodeSystem.Property[String](
      "Formula",
      Some("Formula")
    )

  val MolarMass =
    CodeSystem.Property[String](
      "MolarMass",
      Some("Molar mass")
    )



  override val properties =
    List(
      CASNumber,
      INN,
      TradeNames,
      Synonyms,
      Formula,
      MolarMass
    )  


  override val filters =
    List.empty

  object extensions
  {

    implicit class ASKConceptProperties(val c: CodeSystem.Concept[ASK]) extends AnyVal
    {

      def casNumber =
        c.get(CASNumber).flatMap(_.headOption)

      def inn =
        c.get(INN)

      def tradeNames =
        c.get(TradeNames)

      def synonyms =
        c.get(Synonyms)

      def formula =
        c.get(Formula).flatMap(_.headOption)

      def molarMass =
        c.get(MolarMass).flatMap(_.headOption)
    }

  }

  trait Catalogs[F[_],Env] extends CodeSystemProvider[ASK,F,Env]


  trait CatalogsSPI extends SPIF[
    ({ type Service[F[_]] = Catalogs[F,Applicative[F]] })#Service
  ]


  object Catalogs extends SPILoaderF[CatalogsSPI]

}

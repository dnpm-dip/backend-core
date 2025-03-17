package de.dnpm.dip.coding.hgnc


import cats.Applicative
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
import de.dnpm.dip.coding.{
  Coding,
  Code,
  CodeSystem,
  CodeSystemProvider,
  Ensembl
}


sealed trait HGNC

object HGNC extends CodeSystem.Publisher[HGNC]
{

  implicit val codingSystem: Coding.System[HGNC] =
    Coding.System[HGNC]("https://www.genenames.org/")


  val Name =
    CodeSystem.Property[String](
      "name",
      Some("Gene name")
    )

  val AliasSymbols =
    CodeSystem.Property[String](
      "AliasSymbols",
      Some("Alias symbols for gene")
    )

  val PreviousSymbols =
    CodeSystem.Property[String](
      "PreviousSymbols",
      Some("Previous symbols for gene")
    )

  val EnsemblID =
    CodeSystem.Property[String](
      "EnsemblID",
      Some("EnsemblID")
    )


  override val properties =
    List(
      Name,
      AliasSymbols,
      PreviousSymbols,
      EnsemblID
    )

  override val filters = List.empty


  trait GeneSet[F[_],Env] extends CodeSystemProvider[HGNC,F,Env]


  trait GeneSetSPI extends SPIF[
    ({ type Service[F[_]] = GeneSet[F,Applicative[F]] })#Service
  ]


  object GeneSet extends SPILoaderF[GeneSetSPI]


  object extensions 
  {

    implicit class HGNCConceptProperties(val c: CodeSystem.Concept[HGNC]) extends AnyVal
    {
      def name: String =
        c.get(Name)
         .get.head  // safe, always defined

      def aliasSymbols: Set[String] =
        c.get(AliasSymbols)
         .getOrElse(Set.empty)

      def previousSymbols: Set[String] =
        c.get(PreviousSymbols)
         .getOrElse(Set.empty)

      def ensemblID: Option[Code[Ensembl]] =
        c.get(EnsemblID)
         .flatMap(_.headOption)
         .map(Code[Ensembl](_))
    }

  }

}

package de.dnpm.dip.coding.hgvs


import cats.Applicative
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
import de.dnpm.dip.coding.{
  Coding,
  Code,
  CodeSystem,
  CodeSystemProvider
}


sealed trait HGVS

object HGVS extends CodeSystem.Publisher[HGVS]
{

  implicit val codingSystem: Coding.System[HGVS] =
    Coding.System[HGVS]("https://varnomen.hgvs.org")

  sealed trait DNA
  sealed trait Protein


  implicit val codingSystemDNA: Coding.System[HGVS.DNA] =
    Coding.System[HGVS.DNA]("https://varnomen.hgvs.org/recommendations/DNA/")

  implicit val codingSystemProtein: Coding.System[HGVS.Protein] =
    Coding.System[HGVS.Protein]("https://varnomen.hgvs.org/recommendations/protein/")



  override val properties =
    List.empty

  override val filters =
    List.empty


}

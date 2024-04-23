package de.dnpm.dip.coding.icd


import scala.util.matching.Regex
import cats.Applicative
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider,
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


sealed trait ICDO3 extends ICD

object ICDO3 extends ICDSystem[ICDO3]
{

  sealed trait Topography extends ICDO3
  sealed trait Morphology extends ICDO3

  type T = Topography
  type M = Morphology


  implicit val codingSystem: Coding.System[ICDO3] =
    Coding.System[ICDO3]("urn:oid:2.16.840.1.113883.6.43.1")


  implicit val codingSystemM: Coding.System[ICDO3.M] =
    Coding.System[ICDO3].asInstanceOf[Coding.System[ICDO3.M]]

  implicit val codingSystemT: Coding.System[ICDO3.T] =
    Coding.System[ICDO3].asInstanceOf[Coding.System[ICDO3.T]]



  // Match either C**-C** or C**.*  where * = digit
  private val topographyRegex =
    """(C\d{2}-C\d{2}|C\d{2}(.\d{1})?)""".r

  // Match either ***-*** or ****/*  where * = digit
  private val morphologyRegex =
    """(\d{3}-\d{3}|\d{4}/\d{1})""".r


  val topographyFilter =
    CodeSystem.Filter[ICDO3](
      "topography",
      Some("Filters ICD-O-3-T codings (Topography)"),
      c => topographyRegex matches (c.code.value)
    )

  val morphologyFilter =
    CodeSystem.Filter[ICDO3](
      "morphology",
      Some("Filters ICD-O-3-M codings (Morphology)"),
      c => morphologyRegex matches (c.code.value)
    )

  override val filters =
    super.filters :+ topographyFilter :+ morphologyFilter 



  trait Catalogs[F[_],Env] extends CodeSystemProvider[ICDO3,F,Env]
  {

    def topography(
      version: String
    )(
      implicit env: Env
    ): F[Option[CodeSystem[ICDO3.Topography]]]

    def topography(
      implicit env: Env
    ): F[CodeSystem[ICDO3.Topography]]


    def morphology(
      version: String
    )(
      implicit env: Env
    ): F[Option[CodeSystem[ICDO3.Morphology]]]

    def morphology(
      implicit env: Env
    ): F[CodeSystem[ICDO3.Morphology]]

  }

  trait CatalogsSPI extends SPIF[
    ({ type Service[F[_]] = Catalogs[F,Applicative[F]] })#Service
  ]

  object Catalogs extends SPILoaderF[CatalogsSPI]


}


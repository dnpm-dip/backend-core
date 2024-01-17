package de.dnpm.dip.coding.icd



import scala.util.matching.Regex
import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


sealed trait ICD10GM extends ICD

object ICD10GM extends ICDSystem[ICD10GM]
{

  implicit val codingSystem: Coding.System[ICD10GM] =
    Coding.System[ICD10GM]("http://fhir.de/CodeSystem/bfarm/icd-10-gm")


  object ops
  {
    private val SuperCategory = """[A-Z]\d{2}""".r

    implicit class ICD10GMCodingExtensions(val coding: Coding[ICD10GM]) extends AnyVal
    {

      def superCategory(
        implicit icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]
      ): Option[Coding[ICD10GM]] =
        coding.code.value match {

          case SuperCategory() =>
            Some(coding)

          case _ =>
            coding.version
              .flatMap(icd10gm.get)
              .orElse(Some(icd10gm.latest))
              .flatMap(
                _.parentOf(coding.code)
              )
              .map(_.toCoding)
        }

    }


  }



  trait Catalogs[F[_],Env] extends CodeSystemProvider[ICD10GM,F,Env]

  trait CatalogsSPI extends SPIF[
    ({ type Service[F[_]] = Catalogs[F,Applicative[F]] })#Service
  ]

  object Catalogs extends SPILoaderF[CatalogsSPI]
}


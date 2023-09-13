package de.dnpm.dip.coding.icd


import cats.Applicative
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


}


trait ICD10GMCatalogs[F[_],Env] extends CodeSystemProvider[ICD10GM,F,Env]


trait ICD10GMCatalogsSPI extends SPIF[
  ({ type Service[F[_]] = ICD10GMCatalogs[F,Applicative[F]] })#Service
]


object ICD10GMCatalogs extends SPILoaderF[ICD10GMCatalogsSPI]

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


sealed trait ICD11 extends ICD

object ICD11 extends ICDSystem[ICD11]
{

  implicit val codingSystem: Coding.System[ICD11] =
    Coding.System[ICD11]("http://fhir.de/CodeSystem/bfarm/icd-11")


}


trait ICD11Catalogs[F[_],Env] extends CodeSystemProvider[ICD11,F,Env]


trait ICD11CatalogsSPI extends SPIF[
  ({ type Service[F[_]] = ICD11Catalogs[F,Applicative[F]] })#Service
]


object ICD11Catalogs extends SPILoaderF[ICD11CatalogsSPI]

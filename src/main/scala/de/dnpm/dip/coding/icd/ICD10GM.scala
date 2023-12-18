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


  trait Catalogs[F[_],Env] extends CodeSystemProvider[ICD10GM,F,Env]

  trait CatalogsSPI extends SPIF[
    ({ type Service[F[_]] = Catalogs[F,Applicative[F]] })#Service
  ]

  object Catalogs extends SPILoaderF[CatalogsSPI]
}


package de.dnpm.dip.coding.icd


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


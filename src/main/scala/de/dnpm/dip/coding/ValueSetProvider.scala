package de.dnpm.dip.coding


import java.net.URI
import cats.data.NonEmptyList
import cats.Applicative
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


trait ValueSetProvider[S,F[_],Env]
{
  self =>

  val uri: URI

  def versions(  
    implicit env: Env
  ): F[NonEmptyList[String]]

  def latestVersion(  
    implicit env: Env
  ): F[String]

  def get(
    version: Option[String] = None
  )(
    implicit env: Env
  ): F[Option[ValueSet[S]]]

}

trait ValueSetProviderSPI extends SPIF[
  ({ type Service[F[_]] = ValueSetProvider[Any,F,Applicative[F]] })#Service
]

object ValueSetProvider extends SPILoaderF[ValueSetProviderSPI]

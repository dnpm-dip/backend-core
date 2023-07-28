package de.dnpm.dip.coding



import java.net.URI
import cats.data.NonEmptyList
import cats.Applicative
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


trait CodeSystemProvider[S,F[_],Env]
{
  self =>

  import cats.syntax.functor._

  val uri: URI

  val versionOrdering: Ordering[String]

  def versions(  
    implicit env: Env
  ): F[NonEmptyList[String]]

  def latestVersion(  
    implicit env: Env
  ): F[String]

  def get(
    version: String
  )(
    implicit env: Env
  ): F[Option[CodeSystem[S]]]

  def latest(  
    implicit env: Env
  ): F[CodeSystem[S]]

}

trait CodeSystemProviderSPI extends SPIF[
  ({ type Service[F[_]] = CodeSystemProvider[Any,F,Applicative[F]] })#Service
]

object CodeSystemProvider extends SPILoaderF[CodeSystemProviderSPI]
{

  import scala.language.implicitConversions

  implicit def toAnyCodeSystemProvider[S,Spr >: S,F[_],Env](
    csp: CodeSystemProvider[S,F,Env]
  ): CodeSystemProvider[Spr,F,Env] =
    csp.asInstanceOf[CodeSystemProvider[Spr,F,Env]]

}
